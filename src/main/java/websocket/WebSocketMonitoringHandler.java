package websocket;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import domainmodel.Device;
import lombok.Getter;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.*;

@Getter
@WebSocket
@Singleton
public class WebSocketMonitoringHandler {

    @Inject
    private WebSocketDiscoveryHandler webSocketDiscoveryHandler;

    private Set<Session> sessions = new HashSet<>();

    @Inject
    private Gson gson;

    @OnWebSocketConnect
    public void connected(Session session) throws IOException {
        this.sessions.add(session);

        try {
            Collection<Device> devices = this.webSocketDiscoveryHandler.getSessions().values();

            session.getRemote().sendString(this.gson.toJson(this.fromDevicesToRequest(true, devices)));
        } catch (Exception e) {
            e.printStackTrace();

            session.getRemote().sendString(Integer.toString(HttpStatus.BAD_REQUEST_400));
        }
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        this.sessions.remove(session);
    }

    public List<AddRemoveDeviceRequest> fromDevicesToRequest(boolean add, Collection<Device> devices) {
        List<AddRemoveDeviceRequest> deviceRequests = new ArrayList<>(devices.size());

        for (Device device : devices)
            deviceRequests.add(new AddRemoveDeviceRequest(device, add));

        return deviceRequests;
    }
}
