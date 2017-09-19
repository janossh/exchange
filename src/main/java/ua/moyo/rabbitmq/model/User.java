package ua.moyo.rabbitmq.model;



import ua.moyo.rabbitmq.moyo.Enums.UsersRole;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime date;

    @Column(name = "fordelete")
    private boolean fordelete;

    private boolean registered;

    @Enumerated(EnumType.STRING)
    private UsersRole role;
    @Transient
    private int confirmation_code;
    private String phone;
    private boolean updatetoken;

    public User() {
        this.date = LocalDateTime.now();
    }


    public int getConfirmation_code() {
        return confirmation_code;
    }

    public void setConfirmation_code(int confirmation_code) {
        this.confirmation_code = confirmation_code;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setUsername(String _username) { this.username = _username; }
    public void setPassword(String _password) { this.password = _password; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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



    public boolean isFordelete() {
        return fordelete;
    }

    public void setFordelete(boolean fordelete) {
        this.fordelete = fordelete;
    }


    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public UsersRole getRole() {
        return role;
    }

    public void setRole(UsersRole role) {
        this.role = role;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isUpdatetoken() {
        return updatetoken;
    }

    public void setUpdatetoken(boolean updatetoken) {
        this.updatetoken = updatetoken;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getEmail());
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){return true;}
        if(!(obj instanceof User)){return false;}
        User user = (User)obj;
        if(!email.equals("")){
            return Objects.equals(email, user.getEmail());
        }
        return Objects.equals(email, user.getEmail());

    }

}
