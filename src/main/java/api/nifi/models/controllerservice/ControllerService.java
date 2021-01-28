package api.nifi.models.controllerservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ControllerService {

    /**
     * The id of the component.
     */
    private String id;
    /**
     * The revision for this request/response. The revision is required for any mutable flow requests and is included in all responses.
     */
    private Revision revision;
    /**
     * The revision for this request/response. The revision is required for any mutable flow requests and is included in all responses.
     */
    private String parentGroupId;
    /**
     * The status for this ControllerService. This property is read only.
     */
    private Status status;
    private Component component;
    /**
     * Acknowledges that this node is disconnected to allow for mutable requests to proceed.
     */
    private boolean disconnectedNodeAcknowledged;

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

    public String getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(String parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public boolean isDisconnectedNodeAcknowledged() {
        return disconnectedNodeAcknowledged;
    }

    public void setDisconnectedNodeAcknowledged(boolean disconnectedNodeAcknowledged) {
        this.disconnectedNodeAcknowledged = disconnectedNodeAcknowledged;
    }
}
