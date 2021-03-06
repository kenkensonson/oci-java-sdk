/**
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates. All rights reserved.
 */
package com.oracle.bmc.http;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import java.util.concurrent.TimeUnit;

/**
 * A {@code ClientConfigDecorator} implementation that configure a connection pool for the client for a given
 * {@code ApacheConnectionPoolConfig}.
 *
 * @see ApacheConnectionPoolConfig
 */
@Slf4j
public class ApacheConnectionPoolingClientConfigDecorator implements ClientConfigDecorator {
    private final ApacheConnectionPoolConfig config;

    /** Creates a new {@code ApacheConnectionPoolingClientConfigDecorator} object. */
    public ApacheConnectionPoolingClientConfigDecorator(
            @NonNull final ApacheConnectionPoolConfig config) {
        this.config = config;
    }

    @Override
    public void customizeClientConfig(ClientConfig clientConfig) {
        Validate.notNull(clientConfig, "ClientConfig must not be null");

        // Only configure ApacheConnectorProvider types
        final ConnectorProvider provider = clientConfig.getConnectorProvider();
        Validate.isInstanceOf(
                ApacheConnectorProvider.class,
                provider,
                String.format(
                        "ConnectorProvider of type [%s] is not supported. Expected ApacheConnectorProvider",
                        provider.getClass().getCanonicalName()));

        LOG.info("ApacheConnectionPoolConfig: {}", config);

        final Pair<Integer, TimeUnit> ttl = config.getTtl();
        final PoolingHttpClientConnectionManager connectionManager =
                (ttl != null)
                        ? new PoolingHttpClientConnectionManager(ttl.getLeft(), ttl.getRight())
                        : new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(config.getTotalOpenConnections());
        connectionManager.setDefaultMaxPerRoute(config.getDefaultMaxConnectionsPerRoute());

        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
    }
}
