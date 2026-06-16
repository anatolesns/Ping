package fr.epita.assistants.ping.data.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "users")
@Entity
@Getter
@Setter
public class UserModel {
    // TODO
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "login", unique = true, nullable = false)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "is_admin")
    private Boolean isAdmin;


    public UserModel() {}

    public UserModel(String login, String password, String displayName, String avatar, Boolean isAdmin) {
        
        this.login = login;
        this.password = password;
        this.displayName = displayName;
        this.avatar = avatar;
        this.isAdmin = isAdmin;
    }
}


