package api.nifi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application for deleting unused controller service and cleaning queues
 */
public class ApiRequester {

    public static final String PROPERTIES_FILE = "nifi.properties";
    public static final String NIFI_HOST_PROPERTY_NAME = "nifi.host";
    public static final String BASE_PROCESS_GROUP_ID_PROPERTY_NAME = "basic.process.group.id";
    public static final String INCLUDE_ANCESTOR_GROUPS_PROPERTY_NAME = "include.ancestor.groups";
    public static final String INCLUDE_DESCENDANT_GROUPS_PROPERTY_NAME = "include.descendant.groups";
    public static final String METHOD_PROPERTY_NAME = "method";
    public static final String DELETE_CONTROLLER_SERVICES = "cs";
    public static final String CLEAR_QUEUES = "queue";

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        InputStream resourceAsStream = ApiRequester.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        properties.load(resourceAsStream);
        String nifiHost = properties.getProperty(NIFI_HOST_PROPERTY_NAME);
        String baseProcessGroupID = properties.getProperty(BASE_PROCESS_GROUP_ID_PROPERTY_NAME);
        String method = properties.getProperty(METHOD_PROPERTY_NAME);
        boolean includeAncestorGroups = Boolean.parseBoolean(properties.getProperty(INCLUDE_ANCESTOR_GROUPS_PROPERTY_NAME));
        boolean includeDescendantGroups = Boolean.parseBoolean(properties.getProperty(INCLUDE_DESCENDANT_GROUPS_PROPERTY_NAME));

        if (method.equalsIgnoreCase(DELETE_CONTROLLER_SERVICES)) {
            NiFiSender.deleteAllControllerService(nifiHost, baseProcessGroupID, includeAncestorGroups, includeDescendantGroups);
        } else if (method.equalsIgnoreCase(CLEAR_QUEUES)) {
            NiFiSender.clearAllQueue(nifiHost, baseProcessGroupID);
        } else {
            System.err.println("Method doesn't supported");
        }
    }
}
