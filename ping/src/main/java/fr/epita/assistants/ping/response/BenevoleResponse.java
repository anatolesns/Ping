package fr.epita.assistants.ping.response;

import fr.epita.assistants.ping.data.model.BenevoleModel;

import java.util.List;
import java.util.UUID;

public class BenevoleResponse {
    public UUID id;
    public String nomComplet;
    public Integer age;
    public String telephone;
    public String email;
    public List<String> disponibilites;

    public BenevoleResponse() {}

    public BenevoleResponse(BenevoleModel benevole) {
        this.id = benevole.getId();
        this.nomComplet = benevole.getNomComplet();
        this.age = benevole.getAge();
        this.telephone = benevole.getTelephone();
        this.email = benevole.getEmail();
        this.disponibilites = benevole.getDisponibilites();
    }
}
