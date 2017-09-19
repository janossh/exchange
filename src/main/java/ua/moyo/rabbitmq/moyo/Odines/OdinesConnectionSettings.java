package ua.moyo.rabbitmq.moyo.Odines;


import ua.moyo.rabbitmq.model.Database;

public class OdinesConnectionSettings {

    String host, database, user, password;
    Database database1C;


        public OdinesConnectionSettings(Database database1C, String host, String database, String user, String password) {
        this.database1C = database1C;
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public OdinesConnectionSettings(String database) {
        this.host = "";
        this.database = database;
        this.user = "";
        this.password = "";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Database getDatabase1C() {
        return database1C;
    }

    public void setDatabase1C(Database database1C) {
        this.database1C = database1C;
    }
}
