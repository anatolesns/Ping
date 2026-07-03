package fr.epita.assistants.ping.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Path;

@ApplicationScoped
public class FolderService {

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
     * Résout un chemin relatif par rapport à la racine et retourne null si le
     * résultat sort du répertoire racine.
     */
    public Path resolve(String relativePath) {
        Path root = getRoot();
        String safe = (relativePath == null) ? "" : relativePath;
        Path target = root.resolve(safe).normalize();

        if (!target.startsWith(root)) {
            return null;
        }
        return target;
    }
}