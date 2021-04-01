package api.nifi.models.remotegroups;

import api.nifi.models.controllerservice.Revision;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteProcessGroup {

    private String id;
    private Revision revision;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Revision getRevision() {
        return revision;
    }

    public void setRevision(Revision revision) {
        this.revision = revision;
    }
}
