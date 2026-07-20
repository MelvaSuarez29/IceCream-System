package com.heladeria.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.heladeria.models.DetalleNotaVenta;
import com.heladeria.utils.SupabaseClient;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DetalleNotaVentaDAO {
    private static final Gson gson = new Gson();
    private static final Gson gsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static final String TABLE = "/detalle_factura";

    public static List<DetalleNotaVenta> getByNotaVentaId(String notaVentaId) throws Exception {
        String filter = "factura_id=eq." + URLEncoder.encode(notaVentaId, StandardCharsets.UTF_8);
        String json = SupabaseClient.get(TABLE + "?select=id,factura_id,sabor_id,cantidad,subtotal,sabor:sabor_id(nombre)&" + filter);
        return parseDetalles(json);
    }

    public static DetalleNotaVenta create(DetalleNotaVenta detalle) throws Exception {
        String jsonBody = gsonExpose.toJson(detalle);
        String json = SupabaseClient.post(TABLE, jsonBody);
        Type listType = new TypeToken<List<DetalleNotaVenta>>(){}.getType();
        List<DetalleNotaVenta> list = gson.fromJson(json, listType);
        return list.isEmpty() ? null : list.get(0);
    }

    public static void deleteByNotaVentaId(String notaVentaId) throws Exception {
        String filter = "factura_id=eq." + URLEncoder.encode(notaVentaId, StandardCharsets.UTF_8);
        SupabaseClient.delete(TABLE + "?" + filter);
    }

    private static List<DetalleNotaVenta> parseDetalles(String json) {
        Type listType = new TypeToken<List<DetalleNotaVenta>>(){}.getType();
        List<DetalleNotaVenta> detalles = gson.fromJson(json, listType);

        try {
            var rawList = gson.fromJson(json, List.class);
            for (int i = 0; i < detalles.size(); i++) {
                var raw = (java.util.Map<String, Object>) rawList.get(i);
                if (raw.containsKey("sabor") && raw.get("sabor") instanceof java.util.Map) {
                    var s = (java.util.Map<String, String>) raw.get("sabor");
                    detalles.get(i).setSaborNombre(s.get("nombre"));
                }
            }
        } catch (Exception ignored) {}

        return detalles;
    }
}