package api.nifi.models.controllerservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {

    /**
     * The run status of this ControllerService Allowable values: ENABLED, ENABLING, DISABLED, DISABLING This property is read only.
     */
    private String runStatus;

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }
}
