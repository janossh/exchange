package ua.moyo.rabbitmq.model;

/**
 * Created by JLD on 02.08.2017.
 */
public class DatabaseTube {

    Database database;
    Integer connections;

    public DatabaseTube() {
    }

    public DatabaseTube(Database database, Integer connections) {
        this.database = database;
        this.connections = connections;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Integer getConnections() {
        return connections;
    }

    public void setConnections(Integer connections) {
        this.connections = connections;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseTube that = (DatabaseTube) o;

        return database != null ? database.equals(that.database) : that.database == null;

    }

    @Override
    public int hashCode() {
        return database != null ? database.hashCode() : 0;
    }
}
