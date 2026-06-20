package fr.epita.assistants.ping.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FSEntryResponse {
    public String name;
    public String path;
    public boolean isDirectory;
}
