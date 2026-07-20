package com.heladeria.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DetalleNotaVenta {
    @Expose private String id;

    @Expose
    @SerializedName("factura_id")
    private String notaVentaId;

    @Expose
    @SerializedName("sabor_id")
    private String saborId;

    @Expose private Integer cantidad;
    @Expose private Double subtotal;

    private String saborNombre;

    public DetalleNotaVenta() {}

    public DetalleNotaVenta(String notaVentaId, String saborId, Integer cantidad, Double subtotal) {
        this.notaVentaId = notaVentaId;
        this.saborId = saborId;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNotaVentaId() { return notaVentaId; }
    public void setNotaVentaId(String notaVentaId) { this.notaVentaId = notaVentaId; }
    public String getSaborId() { return saborId; }
    public void setSaborId(String saborId) { this.saborId = saborId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public String getSaborNombre() { return saborNombre; }
    public void setSaborNombre(String saborNombre) { this.saborNombre = saborNombre; }
}
