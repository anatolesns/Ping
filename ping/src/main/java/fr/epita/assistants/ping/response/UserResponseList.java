package fr.epita.assistants.ping.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseList {
    public UUID id;
    public String login;
    public String displayName;
    public Boolean isAdmin;
    public String avatar;
}
