package utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import domainmodel.User;
import spark.Request;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class AuthenticationManager {

    private Map<String, User> users = new HashMap<>();

    @Inject
    public void init() {
        User[] users = new User[]{
                new User("pi-smarthouse@gmail.com", "pi-smarthouse"),
                new User("mouleyslimane@gmail.com", "mouleyslimane"),
                new User("s.mouleyslimane@esi-sba.dz", "s.mouleyslimane")
        };

        for (User user : users)
            this.users.put(user.getEmail(), user);
    }

    public User authenticateUser(Request request, User user) {
        User searchedUser = this.users.get(user.getEmail());

        if (searchedUser != null && user.getPassword() != null &&
                searchedUser.getPassword().equals(user.getPassword()))
            request.session().attribute("user", user);

        return searchedUser;
    }

    public void unAuthenticateUser(Request request) {
        request.session().attribute("user", null);
    }

    public User isUserAuthentified(Request request) {
        return request.session().attribute("user");
    }
}
