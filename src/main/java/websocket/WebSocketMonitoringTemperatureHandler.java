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

@WebSocket
@Singleton
@Getter
public class WebSocketMonitoringTemperatureHandler {

    private Set<Session> sessions = new HashSet<>();

    @Inject
    private Gson gson;

    @OnWebSocketConnect
    public void connected(Session session) throws IOException {
        this.sessions.add(session);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        this.sessions.remove(session);
    }
}
