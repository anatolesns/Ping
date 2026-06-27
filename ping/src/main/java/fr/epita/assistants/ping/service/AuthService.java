package fr.epita.assistants.ping.service;

import fr.epita.assistants.ping.data.model.UserModel;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    public boolean isValidLogin(String login) {
        if (login == null || login.isBlank()) {
            return false;
        }
        int d = 0;
        int u = 0;
        for (int i = 0; i < login.length(); i++) {
            if (login.charAt(i) == '.') {
                if (i == 0 || i == login.length() - 1) {
                    return false;
                }
                d++;
            }
            if (login.charAt(i) == '_') {
                if (i == 0 || i == login.length() - 1) {
                    return false;
                }
                u++;
            }
        }
        return (d == 1 && u == 0) || (u == 1 && d == 0);
    }

    public String generateDisplayName(String login) {
        String separator;
        if (login.contains(".")) {
            separator = "\\.";
        } else {
            separator = "_";
        }
        String[] tab = login.split(separator);
        StringBuilder aux = new StringBuilder();

        for (String part : tab) {
            if (!part.isEmpty()) {
                if (aux.length() > 0) {
                    aux.append(" ");
                }
                String temp = Character.toUpperCase(part.charAt(0)) + part.substring(1);
                aux.append(temp);
            }
        }
        return aux.toString();
    }

    public String generateToken(UserModel user) {
        Set<String> roles = new HashSet<>();
        if (user.getIsAdmin() != null && user.getIsAdmin()) {
            roles.add("admin");
        } else {
            roles.add("user");
        }
        return Jwt.claims()
                .issuer("Groupe7")
                .subject(user.getId().toString())
                .groups(roles)
                .issuedAt(Instant.now().getEpochSecond())
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond())
                .sign();
    }
}
