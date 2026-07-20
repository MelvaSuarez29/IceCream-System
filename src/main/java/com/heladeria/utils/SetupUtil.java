package com.heladeria.utils;

import com.heladeria.config.SupabaseConfig;
import com.heladeria.dao.UsuarioDAO;
import com.heladeria.models.Usuario;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SetupUtil {

    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        seedAdmin();
    }

    public static void seedAdmin() throws Exception {
        Usuario existing = UsuarioDAO.getByEmail("admin@heladeria.com");
        if (existing != null) {
            System.out.println("✓ El admin ya existe: " + existing.getEmail());
            return;
        }

        String hash = PasswordUtil.hash("admin123");
        System.out.println("Hash generado: " + hash);

        String json = """
                {
                    "nombre": "Administrador",
                    "email": "admin@heladeria.com",
                    "password_hash": "%s",
                    "rol": "admin",
                    "activo": true
                }
                """.formatted(hash);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SupabaseConfig.REST_URL + "/usuarios"))
                .header("apikey", SupabaseConfig.SUPABASE_KEY)
                .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            System.out.println("✓ Admin creado exitosamente");
            System.out.println("  Email: admin@heladeria.com");
            System.out.println("  Password: admin123");
        } else {
            System.out.println("✗ Error: " + response.statusCode() + " - " + response.body());
        }
    }
}
