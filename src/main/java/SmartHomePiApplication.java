import com.google.inject.Guice;
import com.google.inject.Injector;
import utils.RoutesRegistrar;
import websocket.WebSocketManager;

public class SmartHomePiApplication {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector();
        injector.getInstance(WebSocketManager.class);
        injector.getInstance(RoutesRegistrar.class);
    }
}
