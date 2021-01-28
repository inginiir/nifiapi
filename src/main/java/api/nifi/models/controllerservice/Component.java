package api.nifi.models.controllerservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Component {

    /**
     * All components referencing this controller service.
     */
    private List<ReferencingComponents> referencingComponents;

    public List<ReferencingComponents> getReferencingComponents() {
        return referencingComponents;
    }

    public void setReferencingComponents(List<ReferencingComponents> referencingComponents) {
        this.referencingComponents = referencingComponents;
    }
}
