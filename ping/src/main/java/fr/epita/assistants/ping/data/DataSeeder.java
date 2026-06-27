package fr.epita.assistants.ping.data;

import java.util.List;
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
        try {
            if (!userRepository.loginExists("admin.admin")) {
                String password = HashUtils.encrypt("admin");
                UserModel admin = new UserModel("admin.admin", password, "Admin Admin", null, true);
                userRepository.save(admin);
            }
            if (!userRepository.loginExists("benevole.benevole")) {
                String se = HashUtils.encrypt("benevole");
                UserModel benevole = new UserModel("benevole.benevole", se, "Benevole Benevole", null, false);
                userRepository.save(benevole);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB null", e);
        }
    }
}
