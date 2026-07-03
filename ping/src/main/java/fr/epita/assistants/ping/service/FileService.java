package fr.epita.assistants.ping.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Path;

@ApplicationScoped
public class FileService {

    @ConfigProperty(name = "filesystem.default.path")
    String rootPath;

    /**
     * Retourne le répertoire racine du système de fichiers géré, sous forme
     * de chemin absolu normalisé.
     */
    public Path getRoot() {
        return Path.of(rootPath).toAbsolutePath().normalize();
    }

    /**
     * Résout un chemin relatif par rapport à la racine en empêchant toute
     * tentative de sortie de celle-ci (path traversal), retournant null si invalide.
     */
    public Path resolve(String relativePath) {
        Path root = getRoot();
        String safe = "";
        if (relativePath != null){
            safe = relativePath;
        }
        if (safe.contains("..")) {
            return null;
        }
        Path target = root.resolve(safe).normalize();

        if (!target.startsWith(root)) {
            return null;
        }
        return target;
    }
}
