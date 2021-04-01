package api.nifi.models.remotegroups;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteProcessGroups {

    private List<RemoteProcessGroup> remoteProcessGroups;

    public List<RemoteProcessGroup> getRemoteProcessGroups() {
        return remoteProcessGroups;
    }

    public void setRemoteProcessGroups(List<RemoteProcessGroup> remoteProcessGroups) {
        this.remoteProcessGroups = remoteProcessGroups;
    }
}
