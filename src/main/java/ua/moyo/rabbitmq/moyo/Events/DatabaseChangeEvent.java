package ua.moyo.rabbitmq.moyo.Events;

import ua.moyo.rabbitmq.model.Database;

public class DatabaseChangeEvent {

    private final Database database;

    public Database getDatabase() {
        return database;
    }

    public DatabaseChangeEvent(Database database) {

        this.database = database;
    }
}
