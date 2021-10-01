/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.reef.wake.remote.impl;

import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.wake.EStage;
import org.apache.reef.wake.EventHandler;
import org.apache.reef.wake.impl.StageManager;
import org.apache.reef.wake.remote.*;
import org.apache.reef.wake.remote.address.LocalAddressProvider;
import org.apache.reef.wake.remote.ports.TcpPortProvider;
import org.apache.reef.wake.remote.transport.Transport;
import org.apache.reef.wake.remote.transport.TransportFactory;

import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default remote manager implementation.
 */
public final class DefaultRemoteManagerImplementation implements RemoteManager {

  private static final Logger LOG = Logger.getLogger(HandlerContainer.class.getName());

  private static final AtomicInteger COUNTER = new AtomicInteger(0);

  /**
   * The timeout used for the execute running in close().
   */
  private static final long CLOSE_EXECUTOR_TIMEOUT = 10000; //ms

  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final RemoteSeqNumGenerator seqGen = new RemoteSeqNumGenerator();

  private final String name;
  private final Transport transport;
  private final RemoteSenderStage reSendStage;
  private final EStage<TransportEvent> reRecvStage;
  private final HandlerContainer handlerContainer;

  private RemoteIdentifier myIdentifier;

  @Inject
  private <T> DefaultRemoteManagerImplementation(
        @Parameter(RemoteConfiguration.ManagerName.class) final String name,
        @Parameter(RemoteConfiguration.HostAddress.class) final String hostAddress,
        @Parameter(RemoteConfiguration.Port.class) final int listeningPort,
        @Parameter(RemoteConfiguration.MessageCodec.class) final Codec<T> codec,
        @Parameter(RemoteConfiguration.ErrorHandler.class) final EventHandler<Throwable> errorHandler,
        @Parameter(RemoteConfiguration.OrderingGuarantee.class) final boolean orderingGuarantee,
        @Parameter(RemoteConfiguration.NumberOfTries.class) final int numberOfTries,
        @Parameter(RemoteConfiguration.RetryTimeout.class) final int retryTimeout,
        final LocalAddressProvider localAddressProvider,
        final TransportFactory tpFactory,
        final TcpPortProvider tcpPortProvider) {

    this.name = name;
    this.handlerContainer = new HandlerContainer<>(name, codec);

    this.reRecvStage = orderingGuarantee ?
        new OrderedRemoteReceiverStage(this.handlerContainer, errorHandler) :
        new RemoteReceiverStage(this.handlerContainer, errorHandler, 10);

    this.transport = tpFactory.newInstance(hostAddress, listeningPort,
        this.reRecvStage, this.reRecvStage, numberOfTries, retryTimeout, tcpPortProvider);

    this.handlerContainer.setTransport(this.transport);

    InetSocketAddress address = new InetSocketAddress(
        localAddressProvider.getLocalAddress(),
        this.transport.getListeningPort());
    this.myIdentifier = new SocketRemoteIdentifier(address);

    this.reSendStage = new RemoteSenderStage(codec, this.transport, 10);

    StageManager.instance().register(this);

    final int counter = COUNTER.incrementAndGet();

    LOG.log(Level.FINEST,
        "RemoteManager {0} instantiated id {1} counter {2} listening on {3} Binding address provided by {4}",
        new Object[] {this.name, this.myIdentifier, counter, this.transport.getLocalAddress(), localAddressProvider});
  }

  /**
   * Returns a proxy event handler for a remote identifier and a message type.
   */
  @Override
  public <T> EventHandler<T> getHandler(
      final RemoteIdentifier destinationIdentifier, final Class<? extends T> messageType) {

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "RemoteManager: {0} destinationIdentifier: {1} messageType: {2}",
          new Object[] {this.name, destinationIdentifier, messageType.getCanonicalName()});
    }

    return new ProxyEventHandler<>(this.myIdentifier, destinationIdentifier,
        "default", this.reSendStage.<T>getHandler(), this.seqGen);
  }

  /**
   * Registers an event handler for a remote identifier and a message type and.
   * returns a subscription
   */
  @Override
  public <T, U extends T> AutoCloseable registerHandler(
      final RemoteIdentifier sourceIdentifier, final Class<U> messageType, final EventHandler<T> theHandler) {

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "RemoteManager: {0} remoteId: {1} messageType: {2} handler: {3}", new Object[] {
          this.name, sourceIdentifier, messageType.getCanonicalName(), theHandler.getClass().getCanonicalName()});
    }

    return this.handlerContainer.registerHandler(sourceIdentifier, messageType, theHandler);
  }

  /**
   * Registers an event handler for a message type and returns a subscription.
   */
  @Override
  public <T, U extends T> AutoCloseable registerHandler(
      final Class<U> messageType, final EventHandler<RemoteMessage<T>> theHandler) {

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "RemoteManager: {0} messageType: {1} handler: {2}", new Object[] {
          this.name, messageType.getCanonicalName(), theHandler.getClass().getCanonicalName()});
    }

    return this.handlerContainer.registerHandler(messageType, theHandler);
  }

  /**
   * Returns my identifier.
   */
  @Override
  public RemoteIdentifier getMyIdentifier() {
    return this.myIdentifier;
  }

  @Override
  public void close() {

    LOG.log(Level.FINE, "RemoteManager: {0} Closing remote manager id: {1}",
        new Object[] {this.name, this.myIdentifier});

    if (!this.closed.compareAndSet(false, true)) {
      LOG.log(Level.FINE, "RemoteManager: {0} already closed", this.name);
      return;
    }

    final Runnable closeRunnable = new Runnable() {
      @Override
      public void run() {

        Thread.currentThread().setName(String.format("CLOSE:RemoteManager:%s:%s", name, myIdentifier));

        try {
          LOG.log(Level.FINE, "Closing sender stage {0}", myIdentifier);
          reSendStage.close();
          LOG.log(Level.FINE, "Closed the remote sender stage");
        } catch (final Exception e) {
          LOG.log(Level.SEVERE, "Unable to close the remote sender stage", e);
        }

        try {
          LOG.log(Level.FINE, "Closing transport {0}", myIdentifier);
          transport.close();
          LOG.log(Level.FINE, "Closed the transport");
        } catch (final Exception e) {
          LOG.log(Level.SEVERE, "Unable to close the transport.", e);
        }

        try {
          LOG.log(Level.FINE, "Closing receiver stage {0}", myIdentifier);
          reRecvStage.close();
          LOG.log(Level.FINE, "Closed the remote receiver stage");
        } catch (final Exception e) {
          LOG.log(Level.SEVERE, "Unable to close the remote receiver stage", e);
        }
      }
    };

    final ExecutorService closeExecutor = Executors.newSingleThreadExecutor();

    closeExecutor.submit(closeRunnable);
    closeExecutor.shutdown();

    if (!closeExecutor.isShutdown()) {
      LOG.log(Level.SEVERE, "close executor did not shutdown properly.");
    }

    final long endTime = System.currentTimeMillis() + CLOSE_EXECUTOR_TIMEOUT;
    while (!closeExecutor.isTerminated()) {
      try {
        final long waitTime = endTime - System.currentTimeMillis();
        closeExecutor.awaitTermination(waitTime, TimeUnit.MILLISECONDS);
      } catch (final InterruptedException e) {
        LOG.log(Level.FINE, "Interrupted", e);
      }
    }

    if (closeExecutor.isTerminated()) {
      LOG.log(Level.FINE, "Close executor terminated properly.");
    } else {
      LOG.log(Level.SEVERE, "Close executor did not terminate properly.");
    }
  }

  @Override
  public String toString() {
    return String.format("RemoteManager: { id: %s handler: %s }", this.myIdentifier, this.handlerContainer);
  }
}
