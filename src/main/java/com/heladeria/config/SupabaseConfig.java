package com.heladeria.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupabaseConfig {
    public static final String SUPABASE_URL;
    public static final String SUPABASE_KEY;
    public static final String SUPABASE_SERVICE_ROLE;
    public static final String REST_URL;

    static {
        Path envPath = Paths.get(System.getProperty("user.dir"), ".env");
        if (!Files.exists(envPath)) {
            throw new ExceptionInInitializerError(".env no encontrado en " + envPath.toAbsolutePath());
        }

        Map<String, String> env = new HashMap<>();
        try {
            for (String line : Files.readAllLines(envPath)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                if (val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
                if (val.endsWith(";")) val = val.substring(0, val.length() - 1);
                if (val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
                env.put(key, val);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Error leyendo .env: " + e.getMessage());
        }

        SUPABASE_URL = env.get("SUPABASE_URL");
        SUPABASE_KEY = env.get("SUPABASE_KEY");
        SUPABASE_SERVICE_ROLE = env.get("SUPABASE_SERVICE_ROLE");

        if (SUPABASE_URL == null || SUPABASE_KEY == null || SUPABASE_SERVICE_ROLE == null) {
            throw new ExceptionInInitializerError(".env debe contener SUPABASE_URL, SUPABASE_KEY y SUPABASE_SERVICE_ROLE");
        }
        REST_URL = SUPABASE_URL + "/rest/v1";
    }
}
