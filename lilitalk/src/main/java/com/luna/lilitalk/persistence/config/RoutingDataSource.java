package com.luna.lilitalk.persistence.config;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected @Nullable Object determineCurrentLookupKey() {
        String dataSourceKey =
            TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "replica" : "source";
        IO.println("Routing to %s dataSource".formatted(dataSourceKey));
        return dataSourceKey;
    }
}
