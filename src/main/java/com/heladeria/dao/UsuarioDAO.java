package com.heladeria.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.heladeria.models.Usuario;
import com.heladeria.utils.SupabaseClient;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UsuarioDAO {
    private static final Gson gson = new Gson();
    private static final Gson gsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static final String TABLE = "/usuarios";

    public static List<Usuario> getAll() throws Exception {
        String json = SupabaseClient.get(TABLE + "?select=*");
        Type listType = new TypeToken<List<Usuario>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    public static List<Usuario> getAllByRol(String rol) throws Exception {
        String filter = URLEncoder.encode("rol", StandardCharsets.UTF_8) + "=eq." + URLEncoder.encode(rol, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=*&" + filter);
        Type listType = new TypeToken<List<Usuario>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    public static Usuario getById(String id) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=*&" + filter);
        Type listType = new TypeToken<List<Usuario>>(){}.getType();
        List<Usuario> list = gson.fromJson(json, listType);
        return list.isEmpty() ? null : list.get(0);
    }

    public static Usuario getByEmail(String email) throws Exception {
        String filter = "email=eq." + URLEncoder.encode(email, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=*&" + filter);
        Type listType = new TypeToken<List<Usuario>>(){}.getType();
        List<Usuario> list = gson.fromJson(json, listType);
        return list.isEmpty() ? null : list.get(0);
    }

    public static Usuario create(Usuario usuario) throws Exception {
        String jsonBody = gsonExpose.toJson(usuario);
        String json = SupabaseClient.post(TABLE, jsonBody);
        Type listType = new TypeToken<List<Usuario>>(){}.getType();
        List<Usuario> list = gson.fromJson(json, listType);
        return list.isEmpty() ? null : list.get(0);
    }

    public static Usuario update(String id, Usuario usuario) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String jsonBody = gsonExpose.toJson(usuario);
        String json = SupabaseClient.patch(TABLE + "?" + filter, jsonBody);
        Type listType = new TypeToken<List<Usuario>>(){}.getType();
        List<Usuario> list = gson.fromJson(json, listType);
        return list.isEmpty() ? null : list.get(0);
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

    public static long countNotasVentaByCliente(String clienteId) throws Exception {
        String filter = "cliente_id=eq." + URLEncoder.encode(clienteId, StandardCharsets.UTF_8);
        String json = SupabaseClient.get("/notas_venta?select=id&" + filter + "&activa=eq.true");
        return gson.fromJson(json, JsonArray.class).size();
    }

    public static Usuario authenticate(String email, String password) throws Exception {
        Usuario user = getByEmail(email);
        if (user == null) return null;
        if (!user.getActivo()) return null;
        if (!com.heladeria.utils.PasswordUtil.verify(password, user.getPasswordHash())) return null;
        return user;
    }
}
