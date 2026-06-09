package com.example.bizflow.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> currentDatabase = new ThreadLocal<>();

    public static void setCurrentDatabase(String database) {
        currentDatabase.set(database);
    }

    public static void clearCurrentDatabase() {
        currentDatabase.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String database = currentDatabase.get();
        return database != null ? database : "auth";
    }
}
