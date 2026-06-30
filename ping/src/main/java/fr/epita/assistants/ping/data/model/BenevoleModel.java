package fr.epita.assistants.ping.data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "benevoles")
@Entity
@Getter
@Setter
public class BenevoleModel {

    @Id
    @Column(name = "id")
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private UserModel user;

    @Column(name = "nom_complet")
    private String nomComplet;

    @Column(name = "age")
    private Integer age;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "email")
    private String email;

    @ElementCollection
    @CollectionTable(name = "benevole_disponibilites", joinColumns = @JoinColumn(name = "benevole_id"))
    @Column(name = "disponibilite")
    private List<String> disponibilites = new ArrayList<>();

    public BenevoleModel() {}

    public BenevoleModel(UserModel user, String nomComplet, Integer age, String telephone, String email) {
        this.user = user;
        this.nomComplet = nomComplet;
        this.age = age;
        this.telephone = telephone;
        this.email = email;
    }
}
