package fr.epita.assistants.ping.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class CreneauService {

    private static final Map<String, String> POSTE_LABELS = Map.of(
            "billetterie", "Billetterie",
            "accueil", "Accueil",
            "buvette", "Buvette",
            "coin-enfant", "Coin Enfant",
            "conference", "Conférence"
    );

    private static final Map<String, String> POSTE_WHATSAPP = Map.of(
            "billetterie", "https://chat.whatsapp.com/F5NTtaQ94GW1pI5JDurCel?s=hd&p=i&mlu=3",
            "accueil",     "https://chat.whatsapp.com/BWVZOnimG8r4F0dWCWORHh?s=hd&p=i&mlu=3",
            "buvette",     "https://chat.whatsapp.com/KxHkqEZSSAd6M86VLER0ZK?s=hd&p=i&mlu=3",
            "coin-enfant", "https://chat.whatsapp.com/IpcvOPKJEfI43cYiRaKKnG?s=hd&p=i&mlu=3",
            "conference",  "https://chat.whatsapp.com/BavrIHxRqos55nuv75Cpp7?s=hd&p=i&mlu=3"
    );

    public String posteLabel(String poste) {
        return POSTE_LABELS.getOrDefault(poste, poste);
    }

    public String posteWhatsapp(String poste) {
        return POSTE_WHATSAPP.getOrDefault(poste, "");
    }
}
