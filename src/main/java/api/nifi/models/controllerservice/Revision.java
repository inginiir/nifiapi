package api.nifi.models.controllerservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Revision {

    /**
     * NiFi employs an optimistic locking strategy where the client must include a revision in their request when performing an update.
     * In a response to a mutable flow request, this field represents the updated base version.
     */
    private Integer version;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
