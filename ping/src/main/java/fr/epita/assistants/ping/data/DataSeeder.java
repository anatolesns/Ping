package fr.epita.assistants.ping.data;

import java.util.List;
import fr.epita.assistants.ping.data.model.UserModel;
import fr.epita.assistants.ping.data.model.CreneauModel;
import fr.epita.assistants.ping.data.model.BenevoleModel;
import fr.epita.assistants.ping.data.repository.UserRepository;
import fr.epita.assistants.ping.data.repository.CreneauRepository;
import fr.epita.assistants.ping.data.repository.BenevoleRepository;
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

    @Inject
    CreneauRepository creneauRepository;

    @Inject
    BenevoleRepository benevoleRepository;

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

                BenevoleModel benevoleProfile = new BenevoleModel(benevole, "benevole", 28, "06 12 34 56 78", "exemple@email.com");
                benevoleRepository.save(benevoleProfile);
            }

            if (creneauRepository.findAll().isEmpty()) {
                creneauRepository.save(new CreneauModel("Vendredi", "08:00", "12:00", "billetterie"));
                creneauRepository.save(new CreneauModel("Vendredi", "12:00", "16:00", "accueil"));
                creneauRepository.save(new CreneauModel("Vendredi", "16:00", "20:00", "buvette"));
                creneauRepository.save(new CreneauModel("Samedi", "08:00", "12:00", "coin-enfant"));
                creneauRepository.save(new CreneauModel("Samedi", "12:00", "16:00", "conference"));
                creneauRepository.save(new CreneauModel("Samedi", "16:00", "20:00", "billetterie"));
            }
        } catch (Exception e) {
            throw new RuntimeException("DB null", e);
        }
    }
}
