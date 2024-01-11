package com.colak.oracle;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@UtilityClass
public class OracleConfig {

    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream input = OracleConfig.class.getResourceAsStream("/oracle.properties")) {
            CONFIG.load(input);
        } catch (IOException exception) {
            log.error("Exception", exception);
        }
    }


    public static String getUser() {
        return CONFIG.getProperty("USER");
    }

    public static String getPassword() {
        return CONFIG.getProperty("PASSWORD");
    }

    public static String getHost() {
        return CONFIG.getProperty("HOST");
    }

    public static int getPort() {
        return Integer.parseInt(CONFIG.getProperty("PORT"));
    }

    public static String getDatabase() {
        return CONFIG.getProperty("DATABASE");
    }

}
