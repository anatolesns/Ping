package fr.epita.assistants.ping.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Path;

@ApplicationScoped
public class FolderService {

    @ConfigProperty(name = "filesystem.default.path")
    String rootPath;

    public Path getRoot() {
        return Path.of(rootPath).toAbsolutePath().normalize();
    }

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