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


    private static String timestamp() {
        return new SimpleDateFormat("dd/MM/yy - HH:mm:ss")
                .format(Calendar.getInstance().getTime());
    }


    public void Log(String message){
        String log = "[" + timestamp() + "] " + message;
        
        if (log_file != null && !log_file.isBlank()) {
        
            try (FileWriter f = new FileWriter(log_file, true);
                PrintWriter write = new PrintWriter(f)) {
                write.println(log);
                return;
            } catch (IOException e) {
                System.out.println(log);
            }
        }

        System.out.println(log);
    }

    public void ErrorLog(String message){
        String log = "[" + timestamp() + "] " + message;
        System.out.println(error_file);
        if (error_file != null && !error_file.isBlank()) {
        
            try (FileWriter f = new FileWriter(error_file, true);
                PrintWriter write = new PrintWriter(f)) {
                write.println(log);
                return;
            } catch (IOException e) {
                System.err.println(log);
            }
        }
        System.err.println(log);
    }
}
