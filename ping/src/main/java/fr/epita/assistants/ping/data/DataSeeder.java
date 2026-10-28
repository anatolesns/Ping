package fr.epita.assistants.ping.data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import fr.epita.assistants.ping.data.model.UserModel;
import fr.epita.assistants.ping.data.model.CreneauModel;
import fr.epita.assistants.ping.data.model.BenevoleModel;
import fr.epita.assistants.ping.data.model.NotificationModel;
import fr.epita.assistants.ping.data.repository.UserRepository;
import fr.epita.assistants.ping.data.repository.CreneauRepository;
import fr.epita.assistants.ping.data.repository.BenevoleRepository;
import fr.epita.assistants.ping.data.repository.NotificationRepository;
import fr.epita.assistants.ping.service.AuthService;
import fr.epita.assistants.ping.service.CreneauService;
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

    @Inject
    NotificationRepository notificationRepository;

    @Inject
    AuthService authService;

    @Inject
    CreneauService creneauService;

    private record DemoBenevole(String login, String nomComplet, int age, String telephone, List<String> disponibilites) {}

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

                BenevoleModel benevoleProfile = new BenevoleModel(benevole, "Camille Martin", 28, "06 12 34 56 78", "camille.martin@example.com");
                benevoleProfile.setDisponibilites(new ArrayList<>(List.of(
                        "Vendredi 12:00-16:00", "Samedi 08:00-12:00", "Samedi 16:00-20:00", "Dimanche 12:00-16:00")));
                benevoleRepository.save(benevoleProfile);
            }

            seedManager(userRepository, "manager.buvette",      "buvette",      "Manager Buvette",      "manager_buvette");
            seedManager(userRepository, "manager.billetterie",   "billetterie",  "Manager Billetterie",  "manager_billetterie");
            seedManager(userRepository, "manager.accueil",       "accueil",      "Manager Accueil",      "manager_accueil");
            seedManager(userRepository, "manager.coinenfant",    "coinenfant",   "Manager Coin Enfant",  "manager_coin-enfant");
            seedManager(userRepository, "manager.conference",    "conference",   "Manager Conference",   "manager_conference");

            if (creneauRepository.findAll().isEmpty()) {
                seedDemo();
            }
        } catch (Exception e) {
            throw new RuntimeException("DB null", e);
        }
    }

    // Full planning (every poste, day and slot), partly taken by benevoles available at that time
    // The demo benevole profiles are spread so that each slot has 3 to 4 candidates for 5 postes
    private void seedDemo() throws Exception {
        List<DemoBenevole> demos = List.of(
                new DemoBenevole("lea.dubois",    "Léa Dubois",    22, "06 21 43 65 87", List.of("Jeudi 08:00-12:00", "Jeudi 12:00-16:00", "Vendredi 16:00-20:00")),
                new DemoBenevole("hugo.bernard",  "Hugo Bernard",  31, "06 32 54 76 98", List.of("Jeudi 16:00-20:00", "Vendredi 08:00-12:00", "Samedi 12:00-16:00")),
                new DemoBenevole("ines.petit",    "Inès Petit",    19, "07 11 23 35 47", List.of("Vendredi 12:00-16:00", "Samedi 08:00-12:00", "Dimanche 16:00-20:00")),
                new DemoBenevole("lucas.moreau",  "Lucas Moreau",  45, "06 58 69 70 81", List.of("Jeudi 08:00-12:00", "Samedi 16:00-20:00", "Dimanche 08:00-12:00")),
                new DemoBenevole("chloe.laurent", "Chloé Laurent", 27, "07 42 53 64 75", List.of("Jeudi 12:00-16:00", "Vendredi 16:00-20:00", "Dimanche 12:00-16:00")),
                new DemoBenevole("nathan.simon",  "Nathan Simon",  24, "06 13 57 91 35", List.of("Jeudi 16:00-20:00", "Samedi 08:00-12:00", "Dimanche 12:00-16:00")),
                new DemoBenevole("manon.michel",  "Manon Michel",  36, "06 24 68 02 46", List.of("Vendredi 08:00-12:00", "Vendredi 12:00-16:00", "Samedi 16:00-20:00")),
                new DemoBenevole("theo.lefebvre", "Théo Lefebvre", 20, "07 35 79 13 57", List.of("Samedi 12:00-16:00", "Dimanche 08:00-12:00", "Dimanche 16:00-20:00")),
                new DemoBenevole("sarah.leroy",   "Sarah Leroy",   52, "06 46 80 24 68", List.of("Jeudi 08:00-12:00", "Samedi 12:00-16:00", "Dimanche 12:00-16:00")),
                new DemoBenevole("louis.roux",    "Louis Roux",    29, "06 57 91 35 79", List.of("Vendredi 08:00-12:00", "Samedi 16:00-20:00", "Dimanche 16:00-20:00")),
                new DemoBenevole("jade.fournier", "Jade Fournier", 23, "07 68 02 46 80", List.of("Jeudi 12:00-16:00", "Vendredi 12:00-16:00", "Dimanche 08:00-12:00")),
                new DemoBenevole("adam.girard",   "Adam Girard",   33, "06 79 13 57 91", List.of("Jeudi 16:00-20:00", "Vendredi 16:00-20:00", "Samedi 08:00-12:00"))
        );

        List<BenevoleModel> benevoles = new ArrayList<>();
        benevoles.add(benevoleRepository.findById(userRepository.findByLogin("benevole.benevole").getId()));
        for (DemoBenevole demo : demos) {
            UserModel user = new UserModel(demo.login(), HashUtils.encrypt("benevole"),
                    authService.generateDisplayName(demo.login()), null, false);
            userRepository.save(user);

            BenevoleModel profile = new BenevoleModel(user, demo.nomComplet(), demo.age(), demo.telephone(), demo.login() + "@example.com");
            profile.setDisponibilites(new ArrayList<>(demo.disponibilites()));
            benevoleRepository.save(profile);
            benevoles.add(profile);
        }

        // Keyed like the disponibilites ("Samedi 08:00-12:00")
        Map<String, List<CreneauModel>> planning = new LinkedHashMap<>();
        for (String jour : List.of("Jeudi", "Vendredi", "Samedi", "Dimanche")) {
            for (String horaire : List.of("08:00-12:00", "12:00-16:00", "16:00-20:00")) {
                String[] heures = horaire.split("-");
                List<CreneauModel> slot = new ArrayList<>();
                for (String poste : List.of("billetterie", "accueil", "buvette", "coin-enfant", "conference")) {
                    slot.add(new CreneauModel(jour, heures[0], heures[1], poste));
                }
                planning.put(jour + " " + horaire, slot);
            }
        }

        // Each benevole takes all availabilities but one, left open for manual assignment
        for (int i = 0; i < benevoles.size(); i++) {
            BenevoleModel benevole = benevoles.get(i);
            List<String> dispos = benevole.getDisponibilites();
            for (int j = 0; j < dispos.size(); j++) {
                if (j == i % dispos.size()) {
                    continue;
                }
                List<CreneauModel> libresSlot = planning.get(dispos.get(j)).stream()
                        .filter(c -> !c.getIsPris())
                        .toList();
                CreneauModel creneau = libresSlot.get(i % libresSlot.size());
                creneau.setIsPris(true);
                creneau.setPrisPar(benevole.getUser());
                seedNotification(benevole.getUser(), "Affectation à un créneau",
                        "Vous avez été assigné au créneau " + creneauLabel(creneau), "CONFIRMATION", 48, true);
            }
        }

        List<CreneauModel> libres = new ArrayList<>();
        for (List<CreneauModel> slot : planning.values()) {
            for (CreneauModel creneau : slot) {
                creneauRepository.save(creneau);
                if (!creneau.getIsPris()) {
                    libres.add(creneau);
                }
            }
        }

        CreneauModel nouveau = libres.get(0);
        seedNotification(benevoles.get(0).getUser(), "Nouveau créneau disponible",
                "Un nouveau créneau à la " + creneauService.posteLabel(nouveau.getPoste()) + " est disponible le "
                        + nouveau.getJour() + " " + nouveau.getHeureDebut() + "-" + nouveau.getHeureFin(),
                "NOUVEAU_CRENEAU", 3, false);

        UserModel admin = userRepository.findByLogin("admin.admin");
        seedNotification(admin, "Désistement d'un créneau",
                benevoles.get(4).getNomComplet() + " a quitté le créneau " + creneauLabel(libres.get(1)), "DESISTEMENT", 1, false);
        seedNotification(admin, "Désistement d'un créneau",
                benevoles.get(8).getNomComplet() + " a quitté le créneau " + creneauLabel(libres.get(2)), "DESISTEMENT", 5, false);
    }

    private String creneauLabel(CreneauModel creneau) {
        return creneau.getJour() + " " + creneau.getHeureDebut() + "-" + creneau.getHeureFin()
                + " - " + creneauService.posteLabel(creneau.getPoste());
    }

    private void seedNotification(UserModel user, String title, String message, String type, int hoursAgo, boolean read) {
        NotificationModel notification = new NotificationModel(user, title, message, type);
        notification.setCreatedAt(Instant.now().minus(hoursAgo, ChronoUnit.HOURS));
        notification.setIsRead(read);
        notificationRepository.save(notification);
    }

    private void seedManager(UserRepository repo, String login, String password, String displayName, String role) {
        try {
            if (!repo.loginExists(login)) {
                UserModel manager = new UserModel(login, HashUtils.encrypt(password), displayName, null, false);
                manager.setRole(role);
                repo.save(manager);
            }
        } catch (Exception e) {
            throw new RuntimeException("Seed manager failed: " + login, e);
        }
    }
}
