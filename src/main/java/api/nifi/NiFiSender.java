package api.nifi;

import api.nifi.models.remotegroups.RemoteProcessGroup;
import api.nifi.models.controllerservice.ControllerService;
import api.nifi.models.controllerservice.ControllerServices;
import api.nifi.models.processgroup.Connection;
import api.nifi.models.processgroup.Connections;
import api.nifi.models.processgroup.ProcessGroup;
import api.nifi.models.processgroup.ProcessGroups;
import api.nifi.models.remotegroups.RemoteProcessGroups;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NiFiSender {

    public static final String DISABLED = "DISABLED";
    public static final String NIFI_PATH_GET_ALL_RG = "/nifi-api/process-groups/%s/remote-process-groups";
    private static final String NIFI_PATH_CHANGE_STATE_RG = "/nifi-api/remote-process-groups/%s/run-status";
    public static String NIFI_PATH_GET_ALL_CS = "/nifi-api/flow/process-groups/%s/controller-services";
    public static String NIFI_PATH_CHANGE_STATE_CS = "/nifi-api/controller-services/%s/run-status";
    public static String NIFI_PATH_DELETE_CS_BY_ID = "/nifi-api/controller-services/%s";
    public static String NIFI_PATH_GET_ALL_PG = "/nifi-api/process-groups/%s/process-groups";
    public static String NIFI_PATH_GET_ALL_CONNECTIONS = "/nifi-api/process-groups/%s/connections";
    public static String NIFI_PATH_CLEAR_QUEUE = "/nifi-api/flowfile-queues/%s/drop-requests";
    public static final String HTTP_PARAM_INCLUDE_ANCESTOR_GROUPS = "includeAncestorGroups";
    public static final String HTTP_PARAM_INCLUDE_DESCENDANT_GROUPS = "includeDescendantGroups";
    public static final String HTTP_PARAM_REVISION_VERSION = "version";
    public static List<ProcessGroup> processGroupsList = new ArrayList<>();

    public static Logger LOGGER;
    public static final String LOGGING_FORMATTER_FORMAT = "java.util.logging.SimpleFormatter.format";
    public static final String LOG_FORMAT = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s: %5$s [%2$s]%6$s%n";

    static {
        System.setProperty(LOGGING_FORMATTER_FORMAT, LOG_FORMAT);
        LOGGER = Logger.getLogger(NiFiSender.class.getName());
    }

    public static void clearAllQueue(String nifiHost, String baseProcessGroupID) {
        getAllProcessGroups(nifiHost, baseProcessGroupID);

        for (ProcessGroup processGroup : processGroupsList) {
            List<Connection> connectionList = getAllConnections(nifiHost, processGroup.getId());
            if (connectionList.size() == 0) {
                LOGGER.info(String.format("There are no connections with queued flow files detected in the process group %s", processGroup.getId()));
            }
            for (Connection connection : connectionList) {
                String id = connection.getId();
                int flowFilesQueued = connection.getStatus().getAggregateSnapshot().getFlowFilesQueued();
                LOGGER.info(String.format("Connection %s contains %d queued flow files", id, flowFilesQueued));
                clearQueue(nifiHost, id, flowFilesQueued);
            }
        }
    }

    private static void getAllProcessGroups(String host, String baseProcessGroupID) {
        processGroupsList.add(new ProcessGroup(baseProcessGroupID));
        ProcessGroups processGroups = new ProcessGroups();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder();
            URI uri = uriBuilder
                    .setScheme(HttpHost.DEFAULT_SCHEME_NAME)
                    .setHost(host)
                    .setPath(String.format(NIFI_PATH_GET_ALL_PG, baseProcessGroupID))
                    .build();
            HttpGet get = new HttpGet(uri);
            try (CloseableHttpResponse response = client.execute(get)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    processGroups = objectMapper.readValue(body, ProcessGroups.class);
                } else {
                    LOGGER.warning(String.format("Error. Http status code %d. Error message: %s", statusCode, body));
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.info("Group don't have another groups");
        }
        for (ProcessGroup processGroup : processGroups.getProcessGroups()) {
            getAllProcessGroups(host, processGroup.getId());
        }
    }

    private static List<Connection> getAllConnections(String nifiHost, String processGroupId) {
        Connections connections = new Connections();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder();
            URI uri = uriBuilder
                    .setScheme(HttpHost.DEFAULT_SCHEME_NAME)
                    .setHost(nifiHost)
                    .setPath(String.format(NIFI_PATH_GET_ALL_CONNECTIONS, processGroupId))
                    .build();
            HttpGet get = new HttpGet(uri);
            try (CloseableHttpResponse response = client.execute(get)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    connections = objectMapper.readValue(body, Connections.class);
                } else {
                    LOGGER.warning(String.format("Error. Http status code %d. Error message: %s", statusCode, body));
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.warning(e.getMessage());
        }
        return connections.getConnections().stream().filter(e -> e.getStatus().getAggregateSnapshot().getFlowFilesQueued() > 0).collect(Collectors.toList());
    }

    private static void clearQueue(String nifiHost, String connectionId, int flowFilesQueued) {
        System.out.printf("Attention! %d queued flow files will be deleted from connection %s. Enter 'yes' to delete, enter anything else to cancel\n", flowFilesQueued, connectionId);
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.next();
        if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                URIBuilder uriBuilder = new URIBuilder();
                URI uri = uriBuilder
                        .setScheme(HttpHost.DEFAULT_SCHEME_NAME)
                        .setHost(nifiHost)
                        .setPath(String.format(NIFI_PATH_CLEAR_QUEUE, connectionId))
                        .build();
                HttpPost post = new HttpPost(uri);
                try (CloseableHttpResponse response = client.execute(post)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_OK) {
                        LOGGER.info(String.format("The queue from connection %s has been cleared. Deleted %d queued flow files", connectionId, flowFilesQueued));
                    } else {
                        LOGGER.warning(String.format("Error. Http status code %d. Error message: %s", statusCode, body));
                    }
                }
            } catch (IOException | URISyntaxException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }


    public static void deleteAllControllerService(String nifiHost, String baseProcessGroupID, boolean includeAncestorGroups, boolean includeDescendantGroups) {
        List<ControllerService> controllerServicesWithoutReferencedComponents = getListOfControllerServiceID(nifiHost, baseProcessGroupID, includeAncestorGroups, includeDescendantGroups);
        deleteControllerServicesWithoutReferencedComponents(nifiHost, controllerServicesWithoutReferencedComponents);
    }

    private static List<ControllerService> getListOfControllerServiceID(
            String host,
            String processGroupId,
            boolean includeAncestorGroups,
            boolean includeDescendantGroups
    ) {
        ControllerServices controllerServices = new ControllerServices();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder();
            URI uri = uriBuilder
                    .setScheme(HttpHost.DEFAULT_SCHEME_NAME)
                    .setHost(host)
                    .setPath(String.format(NIFI_PATH_GET_ALL_CS, processGroupId))
                    .setParameter(HTTP_PARAM_INCLUDE_ANCESTOR_GROUPS, Boolean.toString(includeAncestorGroups))
                    .setParameter(HTTP_PARAM_INCLUDE_DESCENDANT_GROUPS, Boolean.toString(includeDescendantGroups))
                    .build();
            HttpGet get = new HttpGet(uri);
            try (CloseableHttpResponse response = client.execute(get)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    controllerServices = objectMapper.readValue(body, ControllerServices.class);
                } else {
                    LOGGER.warning(String.format("Error. Http status code %d. Error message: %s", statusCode, body));
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.warning(e.getMessage());
        }
        return controllerServices.getControllerServices().stream().filter(e -> e.getComponent().getReferencingComponents().size() == 0).collect(Collectors.toList());
    }

    private static void deleteControllerServicesWithoutReferencedComponents(String host, List<ControllerService> controllerServicesWithoutReferencedComponents) {
        List<ControllerService> controllerServices = new ArrayList<>();
        for (ControllerService controllerServicesWithoutReferencedComponent : controllerServicesWithoutReferencedComponents) {
            String id = controllerServicesWithoutReferencedComponent.getId();
            Integer version = controllerServicesWithoutReferencedComponent.getRevision().getVersion();
            boolean disconnectedNodeAcknowledged = controllerServicesWithoutReferencedComponent.isDisconnectedNodeAcknowledged();
            ControllerService controllerService = changeStateControllerService(host, id, DISABLED, version, disconnectedNodeAcknowledged);
            controllerServices.add(controllerService);
        }

        int size = controllerServices.size();
        if (size == 0) {
            System.out.println("There are no controller services without referenced components");
            System.exit(0);
        }
        System.out.printf("Attention! %d controller services will be deleted. Enter 'yes' to delete, enter anything else to cancel\n", size);
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.next();
        if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {
            for (ControllerService controllerService : controllerServices) {
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    URIBuilder uriBuilder = new URIBuilder();
                    String id = controllerService.getId();
                    URI uri = uriBuilder
                            .setScheme(HttpHost.DEFAULT_SCHEME_NAME)
                            .setHost(host)
                            .setPath(String.format(NIFI_PATH_DELETE_CS_BY_ID, id))
                            .setParameter(HTTP_PARAM_REVISION_VERSION, controllerService.getRevision().getVersion().toString())
                            .build();
                    HttpDelete get = new HttpDelete(uri);
                    try (CloseableHttpResponse response = client.execute(get)) {
                        int statusCode = response.getStatusLine().getStatusCode();
                        String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_OK) {
                            LOGGER.info(String.format("Controller service with ID %s, was successfully deleted", id));
                        } else {
                            LOGGER.warning(String.format("Error. Http status code %d. Error message: %s", statusCode, body));
                        }
                    }
                } catch (IOException | URISyntaxException e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        }
    }

    private static ControllerService changeStateControllerService(String host, String controllerServicesId, String state, Integer version, boolean disconnectedNodeAcknowledged) {
        ControllerService controllerService = new ControllerService();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder();
            URI uri = uriBuilder
                    .setScheme(HttpHost.DEFAULT_SCHEME_NAME)
                    .setHost(host)
                    .setPath(String.format(NIFI_PATH_CHANGE_STATE_CS, controllerServicesId))
                    .build();
            HttpPut put = new HttpPut(uri);
            String bodyJson = String.format("{\"revision\": {\"version\": %d}, \"disconnectedNodeAcknowledged\":%b, \"state\":\"%s\"}", version, disconnectedNodeAcknowledged, state);
            HttpEntity entity = new StringEntity(bodyJson);
            put.setEntity(entity);
            put.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            put.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

            try (CloseableHttpResponse response = client.execute(put)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    controllerService = objectMapper.readValue(body, ControllerService.class);
                    LOGGER.info(String.format("Controller service %s is %s", controllerService.getId(), controllerService.getStatus().getRunStatus()));
                } else {
                    LOGGER.warning(String.format("Error. Http status code %d. Error message: %s", statusCode, body));
                }

            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.warning(e.getMessage());
        }
        return controllerService;
    }

    public static void changeStateAllRemotesGroup(String nifiHost, String processGroupId, String state) {
        getAllProcessGroups(nifiHost, processGroupId);
        processGroupsList.forEach(processGroup -> getRemoteGroups(processGroup.getId(), nifiHost)
                .forEach(remoteProcessGroup -> changeStateRemoteGroup(remoteProcessGroup.getId(), state, remoteProcessGroup.getRevision().getVersion(), nifiHost)));
    }

    private static void changeStateRemoteGroup(String remoteGroupId, String state, Integer version, String nifiHost) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder();
            URI uri = uriBuilder
                    .setScheme(HttpHost.DEFAULT_SCHEME_NAME)
                    .setHost(nifiHost)
                    .setPath(String.format(NIFI_PATH_CHANGE_STATE_RG, remoteGroupId))
                    .build();
            HttpPut put = new HttpPut(uri);
            String bodyJson = String.format("{\"state\": \"%s\",\"revision\": {\"version\": \"%d\"}}", state, version);
            HttpEntity entity = new StringEntity(bodyJson);
            put.setEntity(entity);
            put.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            put.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            try (CloseableHttpResponse response = client.execute(put)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (isSuccess(statusCode)) {
                    LOGGER.info(String.format("Remote group with id %s is %s", remoteGroupId, state));
                } else {
                    LOGGER.warning(String.format("Remote group wasn't change state. Http status code %d. Error message: %s", statusCode, body));
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    private static List<RemoteProcessGroup> getRemoteGroups(String processGroupId, String nifiHost) {
        RemoteProcessGroups remoteProcessGroups = new RemoteProcessGroups();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder();
            URI uri = uriBuilder
                    .setScheme(HttpHost.DEFAULT_SCHEME_NAME)
                    .setHost(nifiHost)
                    .setPath(String.format(NIFI_PATH_GET_ALL_RG, processGroupId))
                    .build();
            HttpGet get = new HttpGet(uri);
            try (CloseableHttpResponse response = client.execute(get)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (isSuccess(statusCode)) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    remoteProcessGroups = objectMapper.readValue(body, RemoteProcessGroups.class);
                } else {
                    LOGGER.warning(String.format("Error. Http status code %d. Error message: %s", statusCode, body));
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.warning(e.getMessage());
        }
        return remoteProcessGroups.getRemoteProcessGroups();
    }

    private static boolean isSuccess(int statusCode) {
        return statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_OK;
    }
}
