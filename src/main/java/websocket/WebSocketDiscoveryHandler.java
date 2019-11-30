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
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.*;

@Getter
@WebSocket
@Singleton
public class WebSocketDiscoveryHandler {

    private Map<Session, Device> sessions = new HashMap<>();

    @Inject
    private Gson gson;

    @Inject
    private WebSocketMonitoringHandler webSocketMonitoringHandler;

    @OnWebSocketConnect
    public void connected(Session session) {
        this.sessions.put(session, new Device("NO_SERIAL_NUMBER", "NO_ASSOCIATED_ROOM", 20));
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) throws IOException {
        Device device = this.sessions.get(session);

        this.sessions.remove(session);

        for (Session monitoringSession : this.webSocketMonitoringHandler.getSessions())
            monitoringSession.getRemote().sendString(
                    this.gson.toJson(this.webSocketMonitoringHandler.fromDevicesToRequest(
                            false, List.of(device))));
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        try {
            Device device = this.gson.fromJson(message, Device.class);

            session.getRemote().sendString(Integer.toString(HttpStatus.OK_200));

            if (!this.sessions.values().contains(device)) {
                this.sessions.put(session, device);

                for (Session monitoringSession : this.webSocketMonitoringHandler.getSessions())
                    monitoringSession.getRemote().sendString(
                            this.gson.toJson(this.webSocketMonitoringHandler.fromDevicesToRequest(
                                    true, List.of(device))));
            }
        } catch (Exception e) {
            e.printStackTrace();

            session.getRemote().sendString(Integer.toString(HttpStatus.BAD_REQUEST_400));
        }
    }
}
