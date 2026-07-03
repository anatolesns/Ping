package fr.epita.assistants.ping.response;

import fr.epita.assistants.ping.data.model.UserModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UserResponse {
    public UUID id;
    public String login;
    public String displayName;
    public Boolean isAdmin;
    public String avatar;

    /**
     * Construit la réponse à partir d'un utilisateur persisté, en n'exposant
     * que les champs destinés au client (sans le mot de passe).
     */
    public UserResponse(UserModel u) {
        this.id = u.getId();
        this.login = u.getLogin();
        this.displayName = u.getDisplayName();
        this.isAdmin = u.getIsAdmin();
        this.avatar = u.getAvatar();
    }
}
