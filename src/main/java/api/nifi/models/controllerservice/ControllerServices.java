package api.nifi.models.controllerservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ControllerServices {

    private List<ControllerService> controllerServices;

    public List<ControllerService> getControllerServices() {
        return controllerServices;
    }

    public void setControllerServices(List<ControllerService> controllerServices) {
        this.controllerServices = controllerServices;
    }
}
