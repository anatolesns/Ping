package fr.epita.assistants.ping.utils;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EmailUtils {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 465;
    private static final String SMTP_USER = "ping.eurexpo@gmail.com";
    private static final String SMTP_PASS = "fwpl eirh xeto imkl";

    /**
     * Envoie au bénévole un e-mail l'informant de son affectation à un créneau donné,
     * avec le lien d'invitation au groupe WhatsApp.
     */
    public static void sendAssignment(String recipient, String nomComplet, String jour, String heureDebut, String heureFin, String poste, String whatsappLink) throws IOException {
        String subject = "Votre affectation au créneau " + jour;
        String body = "Bonjour " + (nomComplet != null ? nomComplet : "") + ",\r\n\r\n"
                + "Vous avez été affecté(e) au créneau suivant :\r\n"
                + "  Poste : " + poste + "\r\n"
                + "  Jour : " + jour + "\r\n"
                + "  Horaire : " + heureDebut + " - " + heureFin + "\r\n\r\n"
                + "Rejoignez le groupe WhatsApp des bénévoles pour rester informé(e) :\r\n"
                + whatsappLink + "\r\n\r\n"
                + "À bientôt,\r\n"
                + "L'équipe Ping";

        sendEmail(recipient, subject, body);
    }

    /**
     * Envoie à un utilisateur un e-mail contenant ses identifiants de connexion.
     */
    public static void sendCredentials(String recipient, String login, String password) throws IOException {
        String subject = "Tes identifiants Ping";
        String body = "Bonjour,\r\n\r\n"
                + "Voici tes identifiants de connexion à Ping :\r\n"
                + "Login : " + login + "\r\n"
                + "Mot de passe : " + password + "\r\n\r\n"
                + "Tu peux te connecter dès maintenant.";

        sendEmail(recipient, subject, body);
    }

    /**
     * Envoie un e-mail via une connexion SMTP over SSL, en dialoguant directement
     * avec le serveur (EHLO, authentification, MAIL FROM, RCPT TO, DATA).
     */
    private static void sendEmail(String recipient, String subject, String body) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(SMTP_HOST, SMTP_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            expect(in, "220");
            command(out, in, "EHLO localhost", "250");
            command(out, in, "AUTH LOGIN", "334");
            command(out, in, Base64.getEncoder().encodeToString(SMTP_USER.getBytes(StandardCharsets.UTF_8)), "334");
            command(out, in, Base64.getEncoder().encodeToString(SMTP_PASS.getBytes(StandardCharsets.UTF_8)), "235");
            command(out, in, "MAIL FROM:<" + SMTP_USER + ">", "250");
            command(out, in, "RCPT TO:<" + recipient + ">", "250");
            command(out, in, "DATA", "354");

            String mail = "From: " + SMTP_USER + "\r\n"
                    + "To: " + recipient + "\r\n"
                    + "Subject: " + subject + "\r\n"
                    + "\r\n"
                    + body + "\r\n.";
            command(out, in, mail, "250");

            command(out, in, "QUIT", "221");
        }
    }

    /**
     * Envoie une commande SMTP au serveur et vérifie que la réponse correspond
     * au code attendu.
     */
    private static void command(PrintWriter out, BufferedReader in, String line, String expectedCode) throws IOException {
        out.print(line + "\r\n");
        out.flush();
        expect(in, expectedCode);
    }

    /**
     * Lit la réponse du serveur SMTP et lève une erreur si le code retourné
     * ne correspond pas à celui attendu.
     */
    private static void expect(BufferedReader in, String expectedCode) throws IOException {
        String line = readResponse(in);
        if (line == null || !line.startsWith(expectedCode)) {
            throw new IOException("SMTP error, expected " + expectedCode + " but got: " + line);
        }
    }

    /**
     * Lit une réponse SMTP potentiellement multi-lignes et retourne sa dernière ligne.
     */
    private static String readResponse(BufferedReader in) throws IOException {
        String line;
        String last;
        do {
            line = in.readLine();
            if (line == null) {
                throw new IOException("SMTP connection closed unexpectedly");
            }
            last = line;
        } while (line.length() > 3 && line.charAt(3) == '-');
        return last;
    }
}
