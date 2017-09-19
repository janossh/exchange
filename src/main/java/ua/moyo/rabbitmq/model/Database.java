package ua.moyo.rabbitmq.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Entity
@Table(name="database1c")
public class Database implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Заполните имя базы данных")
    private String name;
    @NotNull(message = "Заполните ip адрес базы данных")
    private String ip;
    private String comment;
    @Column(name = "active")
    private boolean active;
    @Column(name = "maxconnection")
    private Integer maxConnection;

    @Column(name = "notshop")
    private boolean notShop;
    private String base;
    private String user;
    private String password;
    private String rabbitqueue;

    public Database() {
        this.name = "";
        this.ip = "";
        this.comment = "";
        this.active = true;
        this.maxConnection = 2;
        this.id = 0l;
        this.notShop = false;
        this.base = "";
        this.user = "";
        this.password = "";
        this.rabbitqueue = "";
    }

    public boolean isNotShop() {
        return notShop;
    }

    public void setNotShop(boolean notShop) {
        this.notShop = notShop;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getMaxConnection() {
        return maxConnection;
    }

    public void setMaxConnection(Integer maxConnection) {
        this.maxConnection = maxConnection;
    }

    @Override
    public String toString() {
        return getName() + " ("+getIp()+")";
    }

    public String getPresentation(){
        String presentation;
        if (getRabbitqueue().equals("")){
            presentation =isNotShop() ? getIp()+"_"+getBase() : getIp();
        }
        else {
            presentation = getRabbitqueue();
        }
        return presentation;
    }

    public String getRabbitqueue() {
        return rabbitqueue;
    }

    public void setRabbitqueue(String rabbitqueue) {
        this.rabbitqueue = rabbitqueue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Database database = (Database) o;

        if (ip != null ? !ip.equals(database.ip) : database.ip != null) return false;
        return base != null ? base.equals(database.base) : database.base == null;

    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (base != null ? base.hashCode() : 0);
        return result;
    }


}
