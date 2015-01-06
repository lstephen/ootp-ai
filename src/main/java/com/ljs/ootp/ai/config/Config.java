package com.ljs.ootp.ai.config;

import com.google.common.base.Optional;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author lstephen
 */
public class Config {

    private final Properties properties;

    private Config(Properties properties) {
        this.properties = properties;
    }

    public Optional<String> getValue(String key) {
        return Optional.fromNullable(properties.getProperty(key));
    }

    public static Config createDefault() throws IOException {
        try (
            InputStream in =
                Config.class.getResourceAsStream("/config.properties")) {

            return create(in);
        }
    }

    public static Config create(InputStream in) throws IOException {
        Properties properties = new Properties();

        if (in != null) {
          properties.load(in);
        }

        return new Config(properties);
    }

}
