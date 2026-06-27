package fr.epita.assistants.ping.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    public String password;
    public String displayName;
    public String avatar;
}
