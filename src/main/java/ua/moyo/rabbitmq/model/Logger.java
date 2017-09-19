package ua.moyo.rabbitmq.model;

import ua.moyo.rabbitmq.moyo.Enums.LogPriority;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "log")
public class Logger implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime date;
    private String location;
    private String description;
    @Enumerated(EnumType.STRING)
    private LogPriority priority;
    private boolean showen;

    public Logger() {
    }

    public Logger(String where, String description, LogPriority priority) {
        init(where, description, priority);
    }

    public Logger(String where, String description, boolean show) {
        init(where, description, LogPriority.INFO);
        setShowen(!show);
    }

    private void init(String where, String description, LogPriority priority){
        this.location = where;
        this.description = description;
        this.priority = priority;
        this.date = LocalDateTime.now();
        this.setShowen(false);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LogPriority getPriority() {
        return priority;
    }

    public void setPriority(LogPriority priority) {
        this.priority = priority;
    }

    public boolean isShowen() {
        return showen;
    }

    public boolean isNotShowen() {
        return !showen;
    }

    public void setShowen(boolean showen) {
        this.showen = showen;
    }
}
