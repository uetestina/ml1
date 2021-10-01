/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.buck.android;

import static com.facebook.buck.util.concurrent.MostExecutors.newMultiThreadExecutor;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.facebook.buck.android.device.TargetDeviceOptions;
import com.facebook.buck.android.exopackage.AndroidDevice;
import com.facebook.buck.android.exopackage.AndroidDevicesHelper;
import com.facebook.buck.android.exopackage.ExopackageInfo;
import com.facebook.buck.android.exopackage.ExopackageInstaller;
import com.facebook.buck.android.exopackage.RealAndroidDevice;
import com.facebook.buck.android.toolchain.AndroidPlatformTarget;
import com.facebook.buck.core.build.execution.context.ExecutionContext;
import com.facebook.buck.core.exceptions.BuckUncheckedExecutionException;
import com.facebook.buck.core.exceptions.HumanReadableException;
import com.facebook.buck.core.model.UnconfiguredTargetConfiguration;
import com.facebook.buck.core.sourcepath.resolver.SourcePathResolverAdapter;
import com.facebook.buck.core.toolchain.ToolchainProvider;
import com.facebook.buck.core.util.log.Logger;
import com.facebook.buck.event.BuckEventBus;
import com.facebook.buck.event.ConsoleEvent;
import com.facebook.buck.event.InstallEvent;
import com.facebook.buck.event.SimplePerfEvent;
import com.facebook.buck.event.StartActivityEvent;
import com.facebook.buck.event.UninstallEvent;
import com.facebook.buck.log.GlobalStateManager;
import com.facebook.buck.step.AdbOptions;
import com.facebook.buck.util.Ansi;
import com.facebook.buck.util.Console;
import com.facebook.buck.util.MoreSuppliers;
import com.facebook.buck.util.Scope;
import com.facebook.buck.util.Threads;
import com.facebook.buck.util.concurrent.CommandThreadFactory;
import com.facebook.buck.util.concurrent.MostExecutors;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/** Helper for executing commands over ADB, especially for multiple devices. */
public class AdbHelper implements AndroidDevicesHelper {
  private static final Logger log = Logger.get(AdbHelper.class);
  private static final long ADB_CONNECT_TIMEOUT_MS = 5000;
  private static final long ADB_CONNECT_TIME_STEP_MS = ADB_CONNECT_TIMEOUT_MS / 10;

  /** Pattern that matches safe package names. (Must be a full string match). */
  public static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile("[\\w.-]+");

  private static Optional<Supplier<ImmutableList<AndroidDevice>>> devicesSupplierForTests =
      Optional.empty();

  /**
   * If this environment variable is set, the device with the specified serial number is targeted.
   * The -s option overrides this.
   */
  static final String SERIAL_NUMBER_ENV = "ANDROID_SERIAL";

  /**
   * The next port number to use for communicating with the agent on a device. This resets for every
   * instance of AdbHelper, but is incremented for every device on every call to adbCall().
   */
  private final AtomicInteger nextAgentPort = new AtomicInteger(2828);

  private final AdbOptions options;
  private final TargetDeviceOptions deviceOptions;
  private final ToolchainProvider toolchainProvider;
  private final Supplier<ExecutionContext> contextSupplier;
  private final boolean restartAdbOnFailure;
  private final ImmutableList<String> rapidInstallTypes;
  private final Supplier<ImmutableList<AndroidDevice>> devicesSupplier;

  @Nullable private ListeningExecutorService executorService = null;

  public AdbHelper(
      AdbOptions adbOptions,
      TargetDeviceOptions deviceOptions,
      ToolchainProvider toolchainProvider,
      Supplier<ExecutionContext> contextSupplier,
      boolean restartAdbOnFailure,
      ImmutableList<String> rapidInstallTypes) {
    this.options = adbOptions;
    this.deviceOptions = deviceOptions;
    this.toolchainProvider = toolchainProvider;
    this.contextSupplier = contextSupplier;
    this.restartAdbOnFailure = restartAdbOnFailure;
    this.rapidInstallTypes = rapidInstallTypes;
    this.devicesSupplier = MoreSuppliers.memoize(this::getDevicesImpl);
  }

  @VisibleForTesting
  public static void setDevicesSupplierForTests(
      Optional<Supplier<ImmutableList<AndroidDevice>>> devicesSupplierForTests) {
    AdbHelper.devicesSupplierForTests = devicesSupplierForTests;
  }

  @Override
  public ImmutableList<AndroidDevice> getDevices(boolean quiet) {
    ImmutableList<AndroidDevice> devices = devicesSupplier.get();
    if (!quiet && devices.size() > 1) {
      // Report if multiple devices are matching the filter.
      printMessage("Found " + devices.size() + " matching devices.\n");
    }
    return devices;
  }

  /**
   * Execute an {@link AdbDeviceCallable} for all matching devices. This functions performs device
   * filtering based on three possible arguments:
   *
   * <p>-e (emulator-only) - only emulators are passing the filter -d (device-only) - only real
   * devices are passing the filter -s (serial) - only device/emulator with specific serial number
   * are passing the filter
   *
   * <p>If more than one device matches the filter this function will fail unless multi-install mode
   * is enabled (-x). This flag is used as a marker that user understands that multiple devices will
   * be used to install the apk if needed.
   */
  @SuppressWarnings("PMD.EmptyCatchBlock")
  @Override
  public synchronized void adbCall(String description, AdbDeviceCallable func, boolean quiet)
      throws InterruptedException {
    List<AndroidDevice> devices;

    try (SimplePerfEvent.Scope ignored =
        SimplePerfEvent.scope(getBuckEventBus(), "set_up_adb_call")) {
      devices = getDevices(quiet);
      if (devices.isEmpty()) {
        throw new HumanReadableException("Didn't find any attached Android devices/emulators.");
      }
    }

    // Start executions on all matching devices.
    List<ListenableFuture<Boolean>> futures = new ArrayList<>();
    for (AndroidDevice device : devices) {
      futures.add(
          getExecutorService()
              .submit(
                  () -> {
                    try (SimplePerfEvent.Scope ignored =
                        SimplePerfEvent.scope(
                            getBuckEventBus(),
                            SimplePerfEvent.PerfEventId.of("adbCall " + description),
                            "device_serial",
                            device.getSerialNumber())) {
                      return func.apply(device);
                    }
                  }));
    }

    // Wait for all executions to complete or fail.
    List<Boolean> results;
    try {
      results = Futures.allAsList(futures).get();
    } catch (ExecutionException ex) {
      throw new BuckUncheckedExecutionException(ex.getCause());
    } catch (InterruptedException e) {
      try {
        Futures.allAsList(futures).cancel(true);
      } catch (CancellationException ignored) {
        // Rethrow original InterruptedException instead.
      }
      Threads.interruptCurrentThread();
      throw e;
    }

    int successCount = 0;
    for (Boolean result : results) {
      if (result) {
        successCount++;
      }
    }
    int failureCount = results.size() - successCount;

    // Report results.
    if (successCount > 0 && !quiet) {
      printSuccess(String.format("Successfully ran %s on %d device(s)", description, successCount));
    }

    if (failureCount != 0) {
      throw new HumanReadableException("Failed to %s on %d device(s).", description, failureCount);
    }
  }

  private synchronized ListeningExecutorService getExecutorService() {
    if (executorService != null) {
      return executorService;
    }
    int deviceCount;
    deviceCount = getDevices(true).size();
    int adbThreadCount = options.getAdbThreadCount();
    if (adbThreadCount <= 0) {
      adbThreadCount = deviceCount;
    }
    adbThreadCount = Math.min(deviceCount, adbThreadCount);
    executorService =
        listeningDecorator(
            newMultiThreadExecutor(
                new CommandThreadFactory(
                    getClass().getSimpleName(),
                    GlobalStateManager.singleton().getThreadToCommandRegister()),
                adbThreadCount));
    return executorService;
  }

  private void printMessage(String message) {
    getBuckEventBus().post(ConsoleEvent.info(message));
  }

  private void printSuccess(String successMessage) {
    Ansi ansi = contextSupplier.get().getAnsi();
    getBuckEventBus().post(ConsoleEvent.info(ansi.asHighlightedSuccessText(successMessage)));
  }

  private void printError(String failureMessage) {
    getBuckEventBus().post(ConsoleEvent.severe(failureMessage));
  }

  @Override
  public void installApk(
      SourcePathResolverAdapter pathResolver,
      HasInstallableApk hasInstallableApk,
      boolean installViaSd,
      boolean quiet,
      @Nullable String processName)
      throws InterruptedException {
    InstallEvent.Started started = InstallEvent.started(hasInstallableApk.getBuildTarget());
    if (!quiet) {
      getBuckEventBus().post(started);
    }
    AtomicBoolean success = new AtomicBoolean();
    Set<String> deviceLocales = new HashSet<>();
    try (Scope ignored =
        () -> {
          ImmutableMap.Builder<String, String> deviceInfo = ImmutableMap.builder();
          if (!deviceLocales.isEmpty()) {
            deviceInfo.put(
                InstallEvent.Finished.DEVICE_INFO_LOCALES, Joiner.on(',').join(deviceLocales));
          }
          if (!quiet) {
            getBuckEventBus()
                .post(
                    InstallEvent.finished(
                        started,
                        success.get(),
                        Optional.empty(),
                        Optional.of(
                            AdbHelper.tryToExtractPackageNameFromManifest(
                                pathResolver, hasInstallableApk.getApkInfo())),
                        deviceInfo.build()));
          }
        }) {

      adbCall(
          "Get device locale",
          (device) -> {
            try {
              // It's a bit tortuous to get the locale; there are 6 separate properties
              // we need to check to accurately record this.

              // First try "persist.sys" properties, which are the user's chosen language.
              String locale = device.getProperty("persist.sys.locale");
              // Try persist.sys.language + persist.sys.country
              if (Strings.isNullOrEmpty(locale)) {
                String language = device.getProperty("persist.sys.language");
                if (!Strings.isNullOrEmpty(language)) {
                  String country = device.getProperty("persist.sys.country");
                  if (!Strings.isNullOrEmpty(country)) {
                    locale = language + "-" + country;
                  }
                }
              }
              // Next try ro.product.locale properties which are the default system locale
              if (Strings.isNullOrEmpty(locale)) {
                locale = device.getProperty("ro.product.locale");
              }
              if (Strings.isNullOrEmpty(locale)) {
                String language = device.getProperty("ro.product.locale.language");
                String country = device.getProperty("ro.product.locale.region");

                // Default to en-US if all else fails
                if (Strings.isNullOrEmpty(language)) {
                  language = "en";
                }
                if (Strings.isNullOrEmpty(country)) {
                  country = "US-presumed";
                }
                locale = language = "-" + country;
              }
              deviceLocales.add(locale);
            } catch (Exception e) {
              // Don't log.
            }
            return true;
          },
          true);

      Optional<ExopackageInfo> exopackageInfo = hasInstallableApk.getApkInfo().getExopackageInfo();
      if (exopackageInfo.isPresent()) {
        // TODO(dreiss): Support SD installation.
        installApkExopackage(pathResolver, hasInstallableApk, quiet, processName);
      } else {
        installApkDirectly(pathResolver, hasInstallableApk, installViaSd, quiet);
      }
      success.set(true);
    }
  }

  @Override
  public void startActivity(
      SourcePathResolverAdapter pathResolver,
      HasInstallableApk hasInstallableApk,
      @Nullable String activity,
      boolean waitForDebugger)
      throws IOException {

    // Might need the package name and activities from the AndroidManifest.
    Path pathToManifest =
        pathResolver.getAbsolutePath(hasInstallableApk.getApkInfo().getManifestPath());
    AndroidManifestReader reader =
        DefaultAndroidManifestReader.forPath(
            hasInstallableApk.getProjectFilesystem().resolve(pathToManifest));

    if (activity == null) {
      // Get list of activities that show up in the launcher.
      List<String> launcherActivities = reader.getLauncherActivities();

      // Sanity check.
      if (launcherActivities.isEmpty()) {
        throw new HumanReadableException("No launchable activities found.");
      } else if (launcherActivities.size() > 1) {
        throw new HumanReadableException("Default activity is ambiguous.");
      }

      // Construct a component for the '-n' argument of 'adb shell am start'.
      activity = reader.getPackage() + "/" + launcherActivities.get(0);
    } else if (!activity.contains("/")) {
      // If no package name was provided, assume the one in the manifest.
      activity = reader.getPackage() + "/" + activity;
    }

    String activityToRun = activity;

    printMessage(String.format("Starting activity %s...", activityToRun));

    StartActivityEvent.Started started =
        StartActivityEvent.started(hasInstallableApk.getBuildTarget(), activityToRun);
    getBuckEventBus().post(started);
    try {
      adbCallOrThrow(
          "start activity",
          (device) -> {
            ((RealAndroidDevice) device).deviceStartActivity(activityToRun, waitForDebugger);
            return true;
          },
          false);
      getBuckEventBus().post(StartActivityEvent.finished(started, true));
    } catch (Exception e) {
      getBuckEventBus().post(StartActivityEvent.finished(started, false));
    }
  }

  /**
   * Uninstall apk from all matching devices.
   *
   * @see #installApk(SourcePathResolverAdapter, HasInstallableApk, boolean, boolean, String)
   */
  @Override
  public void uninstallApp(String packageName, boolean shouldKeepUserData)
      throws InterruptedException {
    Preconditions.checkArgument(AdbHelper.PACKAGE_NAME_PATTERN.matcher(packageName).matches());

    UninstallEvent.Started started = UninstallEvent.started(packageName);
    getBuckEventBus().post(started);
    try {
      adbCall(
          "uninstall apk",
          (device) -> {
            ((RealAndroidDevice) device).uninstallApkFromDevice(packageName, shouldKeepUserData);
            return true;
          },
          false);
    } catch (RuntimeException e) {
      getBuckEventBus().post(UninstallEvent.finished(started, false));
      throw e;
    }
    getBuckEventBus().post(UninstallEvent.finished(started, true));
  }

  public static String tryToExtractPackageNameFromManifest(
      SourcePathResolverAdapter pathResolver, HasInstallableApk.ApkInfo apkInfo) {
    Path pathToManifest = pathResolver.getAbsolutePath(apkInfo.getManifestPath());
    return tryToExtractPackageNameFromManifest(pathToManifest);
  }

  static String tryToExtractPackageNameFromManifest(Path pathToManifest) {
    // Note that the file may not exist if AndroidManifest.xml is a generated file
    // and the rule has not been built yet.
    if (!Files.isRegularFile(pathToManifest)) {
      throw new HumanReadableException(
          "Manifest file %s does not exist, so could not extract package name.", pathToManifest);
    }

    try {
      return DefaultAndroidManifestReader.forPath(pathToManifest).getPackage();
    } catch (IOException e) {
      throw new HumanReadableException("Could not extract package name from %s", pathToManifest);
    }
  }

  private BuckEventBus getBuckEventBus() {
    return contextSupplier.get().getBuckEventBus();
  }

  /**
   * Returns list of devices that pass the filter. If there is an invalid combination or no devices
   * are left after filtering this function prints an error and returns null.
   */
  @Nullable
  @VisibleForTesting
  List<IDevice> filterDevices(IDevice[] allDevices) {
    if (allDevices.length == 0) {
      printError("No devices are found.");
      return null;
    }

    List<IDevice> devices = new ArrayList<>();
    Optional<Boolean> emulatorsOnly = Optional.empty();
    if (deviceOptions.isEmulatorsOnlyModeEnabled() && options.isMultiInstallModeEnabled()) {
      emulatorsOnly = Optional.empty();
    } else if (deviceOptions.isEmulatorsOnlyModeEnabled()) {
      emulatorsOnly = Optional.of(true);
    } else if (deviceOptions.isRealDevicesOnlyModeEnabled()) {
      emulatorsOnly = Optional.of(false);
    }

    int onlineDevices = 0;
    for (IDevice device : allDevices) {
      boolean passed = false;
      if (device.isOnline()) {
        onlineDevices++;

        boolean serialMatches = true;
        if (deviceOptions.getSerialNumber().isPresent()) {
          serialMatches = device.getSerialNumber().equals(deviceOptions.getSerialNumber().get());
        } else if (getEnvironment().containsKey(SERIAL_NUMBER_ENV)) {
          serialMatches = device.getSerialNumber().equals(getEnvironment().get(SERIAL_NUMBER_ENV));
        }

        // Only devices of specific type are accepted:
        // either real devices only or emulators only.
        // All online devices match.
        boolean deviceTypeMatches =
            emulatorsOnly
                .map(isEmulatorOnly -> (isEmulatorOnly == createDevice(device).isEmulator()))
                .orElse(true);
        passed = serialMatches && deviceTypeMatches;
      }

      if (passed) {
        devices.add(device);
      }
    }

    // Filtered out all devices.
    if (onlineDevices == 0) {
      printError("No devices are found.");
      return null;
    }

    if (devices.isEmpty()) {
      printError(
          String.format(
              "Found %d connected device(s), but none of them matches specified filter.",
              onlineDevices));
      return null;
    }

    return devices;
  }

  private ImmutableMap<String, String> getEnvironment() {
    return contextSupplier.get().getEnvironment();
  }

  private RealAndroidDevice createDevice(IDevice device) {
    return new RealAndroidDevice(
        getBuckEventBus(),
        device,
        getConsole(),
        getApkFilePathFromProperties().orElse(null),
        nextAgentPort.incrementAndGet(),
        rapidInstallTypes);
  }

  private static boolean isAdbInitialized(AndroidDebugBridge adb) {
    return adb.isConnected() && adb.hasInitialDeviceList();
  }

  /**
   * Creates connection to adb and waits for this connection to be initialized and receive initial
   * list of devices.
   */
  @Nullable
  @SuppressWarnings("PMD.EmptyCatchBlock")
  private static AndroidDebugBridge createAdb(
      AndroidPlatformTarget androidPlatformTarget, ExecutionContext context, int adbTimeout)
      throws InterruptedException {
    DdmPreferences.setTimeOut(adbTimeout);

    try {
      AndroidDebugBridge.init(/* clientSupport */ false);
    } catch (IllegalStateException ex) {
      // ADB was already initialized, we're fine, so just ignore.
    }

    String adbExecutable = androidPlatformTarget.getAdbExecutable().toString();
    log.debug("Using %s to create AndroidDebugBridge", adbExecutable);
    AndroidDebugBridge adb = AndroidDebugBridge.createBridge(adbExecutable, false);
    if (adb == null) {
      context
          .getConsole()
          .printBuildFailure("Failed to connect to adb. Make sure adb server is running.");
      return null;
    }

    long start = System.currentTimeMillis();
    while (!isAdbInitialized(adb)) {
      long timeLeft = start + ADB_CONNECT_TIMEOUT_MS - System.currentTimeMillis();
      if (timeLeft <= 0) {
        break;
      }
      Thread.sleep(ADB_CONNECT_TIME_STEP_MS);
    }
    return isAdbInitialized(adb) ? adb : null;
  }

  private ImmutableList<AndroidDevice> getDevicesImpl() {
    if (devicesSupplierForTests.isPresent()) {
      return devicesSupplierForTests.get().get();
    }

    // TODO(nga): use something else
    UnconfiguredTargetConfiguration toolchainTargetConfiguration =
        UnconfiguredTargetConfiguration.INSTANCE;

    // Initialize adb connection.
    AndroidDebugBridge adb;
    try {
      adb =
          createAdb(
              toolchainProvider.getByName(
                  AndroidPlatformTarget.DEFAULT_NAME,
                  toolchainTargetConfiguration,
                  AndroidPlatformTarget.class),
              contextSupplier.get(),
              options.getAdbTimeout());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (adb == null) {
      // Try resetting state and reconnecting
      printError("Unable to reconnect to existing server, starting a new one");
      try {
        AndroidDebugBridge.disconnectBridge();
        AndroidDebugBridge.terminate();
        adb =
            createAdb(
                toolchainProvider.getByName(
                    AndroidPlatformTarget.DEFAULT_NAME,
                    toolchainTargetConfiguration,
                    AndroidPlatformTarget.class),
                contextSupplier.get(),
                options.getAdbTimeout());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    if (adb == null) {
      printError("Failed to create adb connection.");
      return ImmutableList.of();
    }

    // Build list of matching devices.
    List<IDevice> devices = filterDevices(adb.getDevices());
    // Found multiple devices but multi-install mode is not enabled.
    if (devices != null && devices.size() > 1 && !options.isMultiInstallModeEnabled()) {
      printError(
          String.format(
              "%d device(s) matches specified device filter (1 expected).\n"
                  + "Either disconnect other devices or enable multi-install mode (%s).",
              devices.size(), AdbOptions.MULTI_INSTALL_MODE_SHORT_ARG));
      return ImmutableList.of();
    }

    if (devices == null && restartAdbOnFailure) {
      printError("No devices found with adb, restarting adb-server.");
      adb.restart();
      devices = filterDevices(adb.getDevices());
    }
    if (devices == null && restartAdbOnFailure) {
      printError("No devices found with adb after restart, terminating and restarting adb-server.");
      AndroidDebugBridge.disconnectBridge();
      AndroidDebugBridge.terminate();
      try {
        adb =
            createAdb(
                toolchainProvider.getByName(
                    AndroidPlatformTarget.DEFAULT_NAME,
                    toolchainTargetConfiguration,
                    AndroidPlatformTarget.class),
                contextSupplier.get(),
                options.getAdbTimeout());
        if (adb == null) {
          printError("Failed to re-create adb connection.");
          return ImmutableList.of();
        }
        devices = filterDevices(adb.getDevices());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    if (devices == null) {
      return ImmutableList.of();
    }
    return devices.stream().map(this::createDevice).collect(ImmutableList.toImmutableList());
  }

  private Console getConsole() {
    return contextSupplier.get().getConsole();
  }

  private static Optional<Path> getApkFilePathFromProperties() {
    String apkFileName = System.getProperty("buck.android_agent_path");
    return Optional.ofNullable(apkFileName).map(Paths::get);
  }

  @Override
  public synchronized void close() {
    // getExecutorService() requires the context for lazy initialization, so explicitly check if it
    // has been initialized.
    if (executorService != null) {
      MostExecutors.shutdownOrThrow(
          executorService,
          10,
          TimeUnit.MINUTES,
          new RuntimeException("Failed to shutdown ExecutorService."));
      executorService = null;
    }
  }

  /** An exception that indicates that an executed command returned an unsuccessful exit code. */
  public static class CommandFailedException extends IOException {
    public final String command;
    public final int exitCode;
    public final String output;

    public CommandFailedException(String command, int exitCode, String output) {
      super("Command '" + command + "' failed with code " + exitCode + ".  Output:\n" + output);
      this.command = command;
      this.exitCode = exitCode;
      this.output = output;
    }
  }

  private void installApkExopackage(
      SourcePathResolverAdapter pathResolver,
      HasInstallableApk hasInstallableApk,
      boolean quiet,
      @Nullable String processName)
      throws InterruptedException {
    adbCall(
        "install exopackage apk",
        device ->
            new ExopackageInstaller(
                    pathResolver,
                    contextSupplier.get(),
                    hasInstallableApk.getProjectFilesystem(),
                    tryToExtractPackageNameFromManifest(
                        pathResolver, hasInstallableApk.getApkInfo()),
                    device)
                .doInstall(hasInstallableApk.getApkInfo(), processName),
        quiet);
  }

  private void installApkDirectly(
      SourcePathResolverAdapter pathResolver,
      HasInstallableApk hasInstallableApk,
      boolean installViaSd,
      boolean quiet)
      throws InterruptedException {
    File apk = pathResolver.getAbsolutePath(hasInstallableApk.getApkInfo().getApkPath()).toFile();
    adbCall(
        String.format("install apk %s", hasInstallableApk.getBuildTarget().toString()),
        (device) -> device.installApkOnDevice(apk, installViaSd, quiet),
        quiet);
  }
}
