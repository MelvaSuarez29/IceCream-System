package com.heladeria.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.heladeria.models.NotaVenta;
import com.heladeria.utils.SupabaseClient;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NotaVentaDAO {
    private static final Gson gson = new Gson();
    private static final Gson gsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static final String TABLE = "/facturas";

    public static List<NotaVenta> getAll() throws Exception {
        String json = SupabaseClient.get(TABLE + "?select=*,cliente:cliente_id(nombre),vendedor:vendedor_id(nombre)&activa=eq.true&order=fecha.desc");
        return parseNotas(json);
    }

    public static List<NotaVenta> getByVendedor(String vendedorId) throws Exception {
        String filter = "vendedor_id=eq." + URLEncoder.encode(vendedorId, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=*,cliente:cliente_id(nombre),vendedor:vendedor_id(nombre)&" + filter + "&activa=eq.true&order=fecha.desc");
        return parseNotas(json);
    }

    public static List<NotaVenta> getByCliente(String clienteId) throws Exception {
        String filter = "cliente_id=eq." + URLEncoder.encode(clienteId, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=*,cliente:cliente_id(nombre),vendedor:vendedor_id(nombre)&" + filter + "&activa=eq.true&order=fecha.desc");
        return parseNotas(json);
    }

    public static NotaVenta getById(String id) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=*,cliente:cliente_id(nombre),vendedor:vendedor_id(nombre)&" + filter);
        List<NotaVenta> list = parseNotas(json);
        return list.isEmpty() ? null : list.get(0);
    }

    public static NotaVenta create(NotaVenta nota) throws Exception {
        String jsonBody = gsonExpose.toJson(nota);
        String json = SupabaseClient.post(TABLE, jsonBody);
        Type listType = new TypeToken<List<NotaVenta>>(){}.getType();
        List<NotaVenta> list = gson.fromJson(json, listType);
        return list.isEmpty() ? null : list.get(0);
    }

    public static void updateTotal(String id, double total) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String jsonBody = "{\"total\": " + total + "}";
        SupabaseClient.patch(TABLE + "?" + filter, jsonBody);
    }

    public static void anular(String id) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String jsonBody = "{\"activa\": false}";
        SupabaseClient.patch(TABLE + "?" + filter, jsonBody);
    }

    public static long countActivasByCliente(String clienteId) throws Exception {
        String filter = "cliente_id=eq." + URLEncoder.encode(clienteId, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=id&" + filter + "&activa=eq.true");
        return gson.fromJson(json, JsonArray.class).size();
    }

    private static List<NotaVenta> parseNotas(String json) {
        Type listType = new TypeToken<List<NotaVenta>>(){}.getType();
        List<NotaVenta> notas = gson.fromJson(json, listType);

        try {
            var rawList = gson.fromJson(json, List.class);
            for (int i = 0; i < notas.size(); i++) {
                var raw = (java.util.Map<String, Object>) rawList.get(i);
                if (raw.containsKey("cliente") && raw.get("cliente") instanceof java.util.Map) {
                    var c = (java.util.Map<String, String>) raw.get("cliente");
                    notas.get(i).setClienteNombre(c.get("nombre"));
                }
                if (raw.containsKey("vendedor") && raw.get("vendedor") instanceof java.util.Map) {
                    var v = (java.util.Map<String, String>) raw.get("vendedor");
                    notas.get(i).setVendedorNombre(v.get("nombre"));
                }
            }
        } catch (Exception ignored) {}

        return notas;
    }
}
