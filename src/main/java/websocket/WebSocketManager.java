package websocket;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import spark.Spark;

@Singleton
public class WebSocketManager {

    @Inject
    private WebSocketDiscoveryHandler webSocketDiscoveryHandler;

    @Inject
    private WebSocketMonitoringHandler webSocketMonitoringHandler;

    @Inject
    private WebSocketTemperatureHandler webSocketTemperatureHandler;

    @Inject
    private WebSocketMonitoringTemperatureHandler webSocketMonitoringTemperatureHandler;

    @Inject
    public void init() {
        Spark.webSocket("/discovery", this.webSocketDiscoveryHandler);
        Spark.webSocket("/monitoring-devices-list", this.webSocketMonitoringHandler);
        Spark.webSocket("/temperature", this.webSocketTemperatureHandler);
        Spark.webSocket("/monitoring-temperatures", this.webSocketMonitoringTemperatureHandler);

        Spark.webSocketIdleTimeoutMillis(1000);
    }
}
