package api.nifi.models.processgroup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregateSnapshot {

    private int flowFilesQueued;

    public int getFlowFilesQueued() {
        return flowFilesQueued;
    }

    public void setFlowFilesQueued(int flowFilesQueued) {
        this.flowFilesQueued = flowFilesQueued;
    }
}
