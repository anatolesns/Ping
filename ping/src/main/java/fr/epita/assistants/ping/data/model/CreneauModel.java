package fr.epita.assistants.ping.data.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "creneaux")
@Entity
@Getter
@Setter
public class CreneauModel {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "jour", nullable = false)
    private String jour;

    @Column(name = "heure_debut", nullable = false)
    private String heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private String heureFin;

    @Column(name = "poste", nullable = false)
    private String poste;

    @Column(name = "is_pris")
    private Boolean isPris;

    @ManyToOne
    @JoinColumn(name = "pris_par_id")
    private UserModel prisPar;

    public CreneauModel() {}

    public CreneauModel(String jour, String heureDebut, String heureFin, String poste) {
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.poste = poste;
        this.isPris = false;
        this.prisPar = null;
    }

    public UUID getId() { return id; }
    public String getJour() { return jour; }
    public String getHeureDebut() { return heureDebut; }
    public String getHeureFin() { return heureFin; }
    public String getPoste() { return poste; }
    public Boolean getIsPris() { return isPris; }
    public UserModel getPrisPar() { return prisPar; }

    public void setJour(String jour) { this.jour = jour; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }
    public void setPoste(String poste) { this.poste = poste; }
    public void setIsPris(Boolean isPris) { this.isPris = isPris; }
    public void setPrisPar(UserModel prisPar) { this.prisPar = prisPar; }
}
