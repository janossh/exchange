package ua.moyo.rabbitmq.moyo.Service;


import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.moyo.Enums.LogPriority;

public interface MoYoService {
    void connectionsClose();
    void deleteMessages();
    Integer getTotalDatabaseConnectionForConnect();
    Integer getTotalDatabaseConnectionConnected();
    Integer getTotalDatabaseConnectionConnectedFail();
    void log(String where, String description, LogPriority priority);
    void log(String where, String description, boolean show);
    void logClean();
    void connectDB(Database database);
    void disconnectDB(Database database, boolean inThread);
    void connectAllDB();
    void controlPoolConnections();
    void updateTubesFail(Database database);
    void updateTubesSuccess(Database database);
}
