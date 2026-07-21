package com.proyecto.proyecto_final.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.heladeria.models.Sabor;
import com.heladeria.utils.SupabaseClient;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SaborDAO {
    private static final Gson gson = new Gson();
    private static final Gson gsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static final String TABLE = "/sabores";

    public static List<Sabor> getAll() throws Exception {
        String json = SupabaseClient.get(TABLE + "?select=*&order=nombre.asc");
        Type listType = new TypeToken<List<Sabor>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    public static List<Sabor> getAllActivos() throws Exception {
        String json = SupabaseClient.get(TABLE + "?select=*&activo=eq.true&order=nombre.asc");
        Type listType = new TypeToken<List<Sabor>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    public static Sabor getById(String id) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=*&" + filter);
        Type listType = new TypeToken<List<Sabor>>(){}.getType();
        List<Sabor> list = gson.fromJson(json, listType);
        return list.isEmpty() ? null : list.get(0);
    }

    public static Sabor create(Sabor sabor) throws Exception {
        String jsonBody = gsonExpose.toJson(sabor);
        String json = SupabaseClient.post(TABLE, jsonBody);
        Type listType = new TypeToken<List<Sabor>>(){}.getType();
        List<Sabor> list = gson.fromJson(json, listType);
        return list.isEmpty() ? null : list.get(0);
    }

    public static Sabor update(String id, Sabor sabor) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String jsonBody = gsonExpose.toJson(sabor);
        String json = SupabaseClient.patch(TABLE + "?" + filter, jsonBody);
        Type listType = new TypeToken<List<Sabor>>(){}.getType();
        List<Sabor> list = gson.fromJson(json, listType);
        return list.isEmpty() ? null : list.get(0);
    }

    public static void updateStock(String id, int newStock) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String jsonBody = "{\"stock\": " + newStock + "}";
        SupabaseClient.patch(TABLE + "?" + filter, jsonBody);
    }

    public static void adjustStock(String id, int delta) throws Exception {
        Sabor sabor = getById(id);
        if (sabor != null) {
            int newStock = sabor.getStock() + delta;
            updateStock(id, Math.max(0, newStock));
        }
    }

    public static void updateField(String id, String field, Object value) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String jsonBody = "{\"" + field + "\": " + (value instanceof String ? "\"" + value + "\"" : value) + "}";
        SupabaseClient.patch(TABLE + "?" + filter, jsonBody);
    }

    public static void delete(String id) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        SupabaseClient.delete(TABLE + "?" + filter);
    }
}
