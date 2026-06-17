package fr.epita.assistants.response;

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

    public UserResponse(UserModel u) {
        this.id = u.getId();
        this.login = u.getLogin();
        this.displayName = u.getDisplayName();
        this.isAdmin = u.getIsAdmin();
        this.avatar = u.getAvatar();
    }
}
