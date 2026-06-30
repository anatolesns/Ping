package fr.epita.assistants.ping.response;

import fr.epita.assistants.ping.data.model.CreneauModel;

import java.util.UUID;

public class CreneauResponse {
    public UUID id;
    public String jour;
    public String heureDebut;
    public String heureFin;
    public String poste;
    public Boolean isPris;
    public UUID prisParId;
    public String prisParLogin;

    public CreneauResponse() {}

    public CreneauResponse(CreneauModel creneau) {
        this.id = creneau.getId();
        this.jour = creneau.getJour();
        this.heureDebut = creneau.getHeureDebut();
        this.heureFin = creneau.getHeureFin();
        this.poste = creneau.getPoste();
        this.isPris = creneau.getIsPris();
        if (creneau.getPrisPar() != null) {
            this.prisParId = creneau.getPrisPar().getId();
            this.prisParLogin = creneau.getPrisPar().getLogin();
        }
    }
}
