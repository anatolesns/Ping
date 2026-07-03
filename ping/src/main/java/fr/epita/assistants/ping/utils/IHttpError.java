package fr.epita.assistants.ping.utils;

public interface IHttpError {
    /**
     * Fournit l'exception HTTP correspondant à cette erreur, avec d'éventuels
     * arguments de formatage pour le message.
     */
    RuntimeException get(Object... args);

    /**
     * Lève directement l'exception HTTP correspondant à cette erreur.
     */
    void throwException(Object... args);
}
