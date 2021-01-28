package api.nifi.models.processgroup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessGroup {

    private String id;

    public ProcessGroup() {
    }

    public ProcessGroup(String baseProcessGroupIdPropertyName) {
        this.id = baseProcessGroupIdPropertyName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
