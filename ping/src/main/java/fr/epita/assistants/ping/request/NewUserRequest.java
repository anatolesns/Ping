package fr.epita.assistants.ping.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    public String login;
    public String password;
    public Boolean isAdmin;
    public String role;
}
