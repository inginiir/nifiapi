package api.nifi.models.processgroup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessGroups {

    List<ProcessGroup> processGroups;

    public List<ProcessGroup> getProcessGroups() {
        return processGroups;
    }

    public void setProcessGroups(List<ProcessGroup> processGroups) {
        this.processGroups = processGroups;
    }
}
