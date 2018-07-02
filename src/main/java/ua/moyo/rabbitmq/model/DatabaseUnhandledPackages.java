package ua.moyo.rabbitmq.model;


public class DatabaseUnhandledPackages {

    Database database;
    Integer numberUnhandledMessages;

    public DatabaseUnhandledPackages() {
    }

    public DatabaseUnhandledPackages(Database database, Integer connections) {
        this.database = database;
        this.numberUnhandledMessages = connections;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Integer getNumberUnhandledMessages() {
        return numberUnhandledMessages;
    }

    public void setNumberUnhandledMessages(Integer numberUnhandledMessages) {
        this.numberUnhandledMessages = numberUnhandledMessages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseUnhandledPackages that = (DatabaseUnhandledPackages) o;

        return database != null ? database.equals(that.database) : that.database == null;

    }

    @Override
    public int hashCode() {
        return database != null ? database.hashCode() : 0;
    }
}
