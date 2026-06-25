package fr.epita.assistants.ping.data;

import fr.epita.assistants.ping.data.model.UserModel;
import fr.epita.assistants.ping.data.repository.UserRepository;
import fr.epita.assistants.ping.utils.HashUtils;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DataSeeder {

    @Inject
    UserRepository userRepository;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        if (!userRepository.findAll().isEmpty()) {
            return;
        }

        try {
            String encryptedPassword = HashUtils.encrypt("admin123");
            UserModel admin = new UserModel("admin.admin", encryptedPassword, "Admin Admin", null, true);
            userRepository.save(admin);
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed initial admin user", e);
        }
    }
}
