package configuration;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.google.inject.Singleton;

@Singleton
public class CommonConfigs {

    private Gson gson = new Gson();

    @Provides
    private Gson gson() {
        return this.gson;
    }
}
