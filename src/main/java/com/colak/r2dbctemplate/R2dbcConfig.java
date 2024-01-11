package com.colak.r2dbctemplate;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class R2dbcConfig {

    private static final Properties CONFIG = new Properties();

    public void load(String path) throws IOException {
        try (InputStream input = R2dbcConfig.class.getResourceAsStream(path)) {
            CONFIG.load(input);
        }
    }

    public void load(Properties properties) {
        CONFIG.putAll(properties);

    }

    public String getProtocol() {
        return CONFIG.getProperty("PROTOCOL");
    }

    public String getHost() {
        return CONFIG.getProperty("HOST");
    }

    public int getPort() {
        return Integer.parseInt(CONFIG.getProperty("PORT"));
    }

    public String getDatabase() {
        return CONFIG.getProperty("DATABASE");
    }

    public String getUser() {
        return CONFIG.getProperty("USER");
    }

    public String getPassword() {
        return CONFIG.getProperty("PASSWORD");
    }

}
