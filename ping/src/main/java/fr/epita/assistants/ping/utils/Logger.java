package fr.epita.assistants.ping.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class Logger {
    private static final String RESET_TEXT = "\u001B[0m";
    private static final String RED_TEXT = "\u001B[31m";
    private static final String GREEN_TEXT = "\u001B[32m";

    @ConfigProperty(name = "log_file")
    String log_file;

    @ConfigProperty(name = "error_file")
    String error_file;


    /**
     * Génère l'horodatage formaté utilisé comme préfixe des lignes de log.
     */
    private static String timestamp() {
        return new SimpleDateFormat("dd/MM/yy - HH:mm:ss")
                .format(Calendar.getInstance().getTime());
    }


    /**
     * Journalise un message d'information en vert dans la console et,
     * si configuré, l'ajoute au fichier de log.
     */
    public void Log(String message){
        String log = GREEN_TEXT + "[" + timestamp() + "] " + message + RESET_TEXT;

        if (log_file != null && !log_file.isBlank()) {
            try (FileWriter f = new FileWriter(log_file, true);
                PrintWriter write = new PrintWriter(f)) {
                write.println(log);
                System.out.println(log);
                return;
            } catch (IOException e) {
                System.out.println(log);
            }
        }

        System.out.println(log);
    }

    /**
     * Journalise un message d'erreur en rouge dans la console et,
     * si configuré, l'ajoute au fichier d'erreurs.
     */
    public void ErrorLog(String message){
        String log = RED_TEXT + "[" + timestamp() + "] " + message + RESET_TEXT;

        if (error_file != null && !error_file.isBlank()) {
            try (FileWriter f = new FileWriter(error_file, true);
                PrintWriter write = new PrintWriter(f)) {
                write.println(log);
                System.err.println(log);
                return;
            } catch (IOException e) {
                System.err.println(log);
            }
        }
        System.err.println(log);
    }
}
