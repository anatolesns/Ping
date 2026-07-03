package fr.epita.assistants.ping.service;

import fr.epita.assistants.ping.data.model.UserModel;
import fr.epita.assistants.ping.utils.EmailUtils;
import fr.epita.assistants.ping.utils.HashUtils;
import fr.epita.assistants.ping.utils.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BenevoleService {

    @Inject
    Logger logger;

    public void sendCredentialsEmail(String email, UserModel user) {
        try {
            String plainPassword = HashUtils.decrypt(user.getPassword());
            EmailUtils.sendCredentials(email, user.getLogin(), plainPassword);
            logger.Log("PUT /api/benevole, credentials email sent to " + email + " for login = " + user.getLogin());
        } catch (Exception e) {
            logger.ErrorLog("PUT /api/benevole, send credentials email failed for login = " + user.getLogin() + " , error = " + e.getMessage());
        }
    }
}
