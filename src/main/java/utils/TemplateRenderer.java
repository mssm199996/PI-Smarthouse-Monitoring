package utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import domainmodel.User;
import lombok.Getter;
import lombok.ToString;
import spark.ModelAndView;
import spark.Request;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class TemplateRenderer {

    public static ThymeleafTemplateEngine THYMELEAF_TEMPLATE_ENGINE = new ThymeleafTemplateEngine();

    public ModelAndView login() {
        return new ModelAndView(new HashMap<>(), Pages.LOGIN.getPath());
    }

    public ModelAndView instantState(User user, Request request) {
        Map<String, Object> params = new HashMap<>();

        this.addUserAttribute(user, params);
        this.addRequestAttribute(request, params);

        return new ModelAndView(params, Pages.INSTANT_STATE.getPath());
    }

    public ModelAndView availableDevices(User user, Request request) {
        Map<String, Object> params = new HashMap<>();

        this.addUserAttribute(user, params);
        this.addRequestAttribute(request, params);

        return new ModelAndView(params, Pages.AVAILABLE_DEVICES.getPath());
    }

    private void addRequestAttribute(Request request, Map<String, Object> model) {
        model.put("userRequest", request);
    }

    private void addUserAttribute(User user, Map<String, Object> model) {
        model.put("user", user);
    }

    @Getter
    @ToString
    public enum Pages {
        LOGIN("login"),
        INSTANT_STATE("instant-state"),
        AVAILABLE_DEVICES("available-devices");

        private String path;

        Pages(String path) {
            this.path = path;
        }
    }
}
