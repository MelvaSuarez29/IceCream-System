package com.heladeria.utils;

import com.heladeria.config.SupabaseConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SupabaseClient {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static HttpRequest.Builder baseRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(SupabaseConfig.REST_URL + path))
                .header("apikey", SupabaseConfig.SUPABASE_KEY)
                .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation");
    }

    public static String get(String path) throws Exception {
        HttpRequest request = baseRequest(path).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("GET " + path + " -> " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    public static String post(String path, String jsonBody) throws Exception {
        HttpRequest request = baseRequest(path)
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("POST " + path + " -> " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    public static String patch(String path, String jsonBody) throws Exception {
        HttpRequest request = baseRequest(path)
                .header("Prefer", "return=representation")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("PATCH " + path + " -> " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    public static String delete(String path) throws Exception {
        HttpRequest request = baseRequest(path)
                .header("Prefer", "return=minimal")
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("DELETE " + path + " -> " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }
}
