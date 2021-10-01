package com.hubspot.baragon.agent.config;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class LoadBalancerConfiguration {
  public static final int DEFAULT_COMMAND_TIMEOUT_MS = 10000;

  @NotNull
  private String name;

  private String defaultDomain;

  @Deprecated
  private String domain;

  @NotNull
  private String rootPath;

  @NotNull
  private String checkConfigCommand;

  @NotNull
  private String reloadConfigCommand;

  private Optional<String> logRotateCommand = Optional.absent();

  private long rotateIntervalMillis = TimeUnit.HOURS.toMillis(1);

  @Min(0)
  private int commandTimeoutMs = DEFAULT_COMMAND_TIMEOUT_MS;

  @NotNull
  private Set<String> domains = Collections.emptySet();

  @NotNull
  private Map<String, Set<String>> domainAliases = Collections.emptyMap();

  @NotNull
  @Min(1)
  private int maxLbWorkerCount = 1;

  @NotNull
  private Optional<String> workerCountCommand = Optional.absent();

  private boolean limitWorkerCount = false;

  private boolean turnOffPurgeableCacheInTemplates = false;

  @NotNull
  @Min(1)
  private int minHealthyAgents = 1;

  @NotNull
  private Set<String> servicesToBlockFromPurgeableCache = Collections.emptySet();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public String getCheckConfigCommand() {
    return checkConfigCommand;
  }

  public void setCheckConfigCommand(String checkConfigCommand) {
    this.checkConfigCommand = checkConfigCommand;
  }

  public String getReloadConfigCommand() {
    return reloadConfigCommand;
  }

  public void setReloadConfigCommand(String reloadConfigCommand) {
    this.reloadConfigCommand = reloadConfigCommand;
  }

  public int getCommandTimeoutMs() {
    return commandTimeoutMs;
  }

  public void setCommandTimeoutMs(int commandTimeoutMs) {
    this.commandTimeoutMs = commandTimeoutMs;
  }

  public Optional<String> getDefaultDomain() {
    return Optional
      .fromNullable(Strings.emptyToNull(defaultDomain))
      .or(Optional.fromNullable(Strings.emptyToNull(domain)));
  }

  public void setDefaultDomain(String defaultDomain) {
    this.defaultDomain = defaultDomain;
  }

  @Deprecated
  public Optional<String> getDomain() {
    return getDefaultDomain();
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public Set<String> getDomains() {
    return domains;
  }

  public void setDomains(Set<String> domains) {
    this.domains = domains;
  }

  public Map<String, Set<String>> getDomainAliases() {
    return domainAliases;
  }

  public void setDomainAliases(Map<String, Set<String>> domainAliases) {
    this.domainAliases = domainAliases;
  }

  public int getMaxLbWorkerCount() {
    return maxLbWorkerCount;
  }

  public void setMaxLbWorkerCount(int maxLbWorkerCount) {
    this.maxLbWorkerCount = maxLbWorkerCount;
  }

  public Optional<String> getWorkerCountCommand() {
    return workerCountCommand;
  }

  public void setWorkerCountCommand(Optional<String> workerCountCommand) {
    this.workerCountCommand = workerCountCommand;
  }

  public boolean isLimitWorkerCount() {
    return limitWorkerCount;
  }

  public void setLimitWorkerCount(boolean limitWorkerCount) {
    this.limitWorkerCount = limitWorkerCount;
  }

  public Optional<String> getLogRotateCommand() {
    return logRotateCommand;
  }

  public void setLogRotateCommand(Optional<String> logRotateCommand) {
    this.logRotateCommand = logRotateCommand;
  }

  public long getRotateIntervalMillis() {
    return rotateIntervalMillis;
  }

  public void setRotateIntervalMillis(long rotateIntervalMillis) {
    this.rotateIntervalMillis = rotateIntervalMillis;
  }

  public boolean isTurnOffPurgeableCacheInTemplates() {
    return turnOffPurgeableCacheInTemplates;
  }

  public void setTurnOffPurgeableCacheInTemplates(
    boolean turnOffPurgeableCacheInTemplates
  ) {
    this.turnOffPurgeableCacheInTemplates = turnOffPurgeableCacheInTemplates;
  }

  public Set<String> getServicesToBlockFromPurgeableCache() {
    return servicesToBlockFromPurgeableCache;
  }

  public void setServicesToBlockFromPurgeableCache(
    Set<String> servicesToBlockFromPurgeableCache
  ) {
    this.servicesToBlockFromPurgeableCache = servicesToBlockFromPurgeableCache;
  }

  public int getMinHealthyAgents() {
    return minHealthyAgents;
  }

  public void setMinHealthyAgents(int minHealthyAgents) {
    this.minHealthyAgents = minHealthyAgents;
  }
}
