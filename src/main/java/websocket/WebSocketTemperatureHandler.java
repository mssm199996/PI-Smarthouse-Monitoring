package websocket;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import domainmodel.Device;
import domainmodel.TemperatureValue;
import lombok.Getter;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

@Getter
@WebSocket
@Singleton
public class WebSocketTemperatureHandler {

    @Inject
    private Gson gson;

    @Inject
    private WebSocketDiscoveryHandler webSocketDiscoveryHandler;

    @Inject
    private WebSocketMonitoringTemperatureHandler webSocketMonitoringTemperatureHandler;

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        try {
            Collection<Device> devices = this.webSocketDiscoveryHandler.getSessions().values();
            TemperatureValue temperatureValue = this.gson.fromJson(message, TemperatureValue.class);

            if (temperatureValue != null) {
                Device device = this.filterDevicesBySerialNumber(devices, temperatureValue.getSerialNumber());

                if (device != null) {
                    if (temperatureValue.getValue() > device.getLimit())
                        session.getRemote().sendString(Integer.toString(HttpStatus.CONFLICT_409));
                    else session.getRemote().sendString(Integer.toString(HttpStatus.OK_200));

                    temperatureValue.setInstant(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

                    for (Session monitoringSession : this.webSocketMonitoringTemperatureHandler.getSessions())
                        monitoringSession.getRemote().sendString(this.gson.toJson(temperatureValue));

                } else session.getRemote().sendString(Integer.toString(HttpStatus.BAD_REQUEST_400));
            } else
                session.getRemote().sendString(Integer.toString(HttpStatus.BAD_REQUEST_400));
        } catch (Exception e) {
            e.printStackTrace();

            session.getRemote().sendString(Integer.toString(HttpStatus.BAD_REQUEST_400));
        }
    }

    private Device filterDevicesBySerialNumber(Collection<Device> devices, String serialNumber) {
        for (Device device : devices)
            if (device.getSerialNumber().equals(serialNumber))
                return device;

        return null;
    }
}
