package utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import domainmodel.User;
import lombok.Getter;
import lombok.ToString;
import spark.Redirect;
import spark.Spark;

import java.nio.file.Paths;

@Singleton
public class RoutesRegistrar {

    @Inject
    private TemplateRenderer templateRenderer;

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    public void init() {
        this.registerStaticResources();

        this.registerLoginRoute();
        this.registerLoginProcessingRoute();

        this.registrerLogoutProcessingRoute();

        this.registerInstantStateRoute();
        this.registerAvailableDevicesRoute();
    }

    private void registerStaticResources() {
        Spark.staticFileLocation("/public");
    }

    private void registerLoginRoute() {
        Spark.get(Paths.INDEX.getPath(), (request, response) -> {
                    User user = this.authenticationManager.isUserAuthentified(request);

                    if (user != null) {
                        response.redirect(Paths.AVAILABLE_DEVICES.getPath());

                        return null;
                    } else return this.templateRenderer.login();
                },
                TemplateRenderer.THYMELEAF_TEMPLATE_ENGINE);
    }

    private void registerLoginProcessingRoute() {
        Spark.post(Paths.LOGIN_PROCESSING.getPath(), (request, response) -> {
            User user = this.authenticationManager.isUserAuthentified(request);

            if (user != null)
                response.redirect(Paths.AVAILABLE_DEVICES.getPath());
            else {
                String email = request.queryParams("email");
                String password = request.queryParams("password");

                this.authenticationManager.authenticateUser(request, new User(email, password));

                response.redirect(Paths.INDEX.getPath());
            }

            return null;
        });
    }

    private void registrerLogoutProcessingRoute() {
        Spark.get(Paths.LOGOUT_PROCESSING.getPath(), (request, response) -> {
            this.authenticationManager.unAuthenticateUser(request);

            response.redirect(Paths.INDEX.getPath());

            return null;
        });
    }

    private void registerInstantStateRoute() {
        Spark.get(Paths.INSTANT_STATE.getPath(), (request, response) -> {
            User user = this.authenticationManager.isUserAuthentified(request);

            if (user != null) {
                return this.templateRenderer.instantState(user, request);
            } else {
                response.redirect(Paths.INDEX.getPath());

                return null;
            }
        }, TemplateRenderer.THYMELEAF_TEMPLATE_ENGINE);
    }

    private void registerAvailableDevicesRoute() {
        Spark.get(Paths.AVAILABLE_DEVICES.getPath(), (request, response) -> {
            User user = this.authenticationManager.isUserAuthentified(request);

            if (user != null) {
                return this.templateRenderer.availableDevices(user, request);
            } else {
                response.redirect(Paths.INDEX.getPath());

                return null;
            }
        }, TemplateRenderer.THYMELEAF_TEMPLATE_ENGINE);
    }

    @Getter
    @ToString
    enum Paths {
        INDEX("/"),

        LOGIN_PROCESSING("/login-processing"),
        LOGOUT_PROCESSING("/logout-processing"),

        INSTANT_STATE("/dashboard/instant-state"),
        AVAILABLE_DEVICES("/dashboard/available-devices");

        private String path;

        Paths(String path) {
            this.path = path;
        }
    }
}
