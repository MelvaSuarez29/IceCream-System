package com.proyecto.proyecto_final.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.heladeria.models.Factura;
import com.heladeria.utils.SupabaseClient;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FacturaDAO {
    private static final Gson gson = new Gson();
    private static final Gson gsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static final String TABLE = "/facturas";

    public static List<Factura> getAll() throws Exception {
        String json = SupabaseClient.get(TABLE + "?select=*,cliente:cliente_id(nombre),vendedor:vendedor_id(nombre)&order=fecha.desc");
        return parseFacturas(json);
    }

    public static List<Factura> getByVendedor(String vendedorId) throws Exception {
        String filter = "vendedor_id=eq." + URLEncoder.encode(vendedorId, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=*,cliente:cliente_id(nombre),vendedor:vendedor_id(nombre)&" + filter + "&order=fecha.desc");
        return parseFacturas(json);
    }

    public static List<Factura> getByCliente(String clienteId) throws Exception {
        String filter = "cliente_id=eq." + URLEncoder.encode(clienteId, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=*,cliente:cliente_id(nombre),vendedor:vendedor_id(nombre)&" + filter + "&order=fecha.desc");
        return parseFacturas(json);
    }

    public static Factura getById(String id) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=*,cliente:cliente_id(nombre),vendedor:vendedor_id(nombre)&" + filter);
        List<Factura> list = parseFacturas(json);
        return list.isEmpty() ? null : list.get(0);
    }

    public static Factura create(Factura factura) throws Exception {
        String jsonBody = gsonExpose.toJson(factura);
        String json = SupabaseClient.post(TABLE, jsonBody);
        Type listType = new TypeToken<List<Factura>>(){}.getType();
        List<Factura> list = gson.fromJson(json, listType);
        return list.isEmpty() ? null : list.get(0);
    }

    public static void updateTotal(String id, double total) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        String jsonBody = "{\"total\": " + total + "}";
        SupabaseClient.patch(TABLE + "?" + filter, jsonBody);
    }

    public static void delete(String id) throws Exception {
        String filter = "id=eq." + URLEncoder.encode(id, StandardCharsets.UTF_8);
        SupabaseClient.delete(TABLE + "?" + filter);
    }

    private static List<Factura> parseFacturas(String json) {
        Type listType = new TypeToken<List<Factura>>(){}.getType();
        List<Factura> facturas = gson.fromJson(json, listType);

        try {
            var rawList = gson.fromJson(json, List.class);
            for (int i = 0; i < facturas.size(); i++) {
                var raw = (java.util.Map<String, Object>) rawList.get(i);
                if (raw.containsKey("cliente") && raw.get("cliente") instanceof java.util.Map) {
                    var c = (java.util.Map<String, String>) raw.get("cliente");
                    facturas.get(i).setClienteNombre(c.get("nombre"));
                }
                if (raw.containsKey("vendedor") && raw.get("vendedor") instanceof java.util.Map) {
                    var v = (java.util.Map<String, String>) raw.get("vendedor");
                    facturas.get(i).setVendedorNombre(v.get("nombre"));
                }
            }
        } catch (Exception ignored) {}

        return facturas;
    }
}
