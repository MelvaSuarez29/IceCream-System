package com.heladeria.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotaVenta {
    @Expose private String id;
    @Expose private String fecha;

    @Expose
    @SerializedName("cliente_id")
    private String clienteId;

    @Expose
    @SerializedName("vendedor_id")
    private String vendedorId;

    @Expose private Double total;

    @Expose private Boolean activa;

    private String clienteNombre;
    private String vendedorNombre;

    public NotaVenta() {
        this.activa = true;
    }

    public NotaVenta(String clienteId, String vendedorId) {
        this.clienteId = clienteId;
        this.vendedorId = vendedorId;
        this.total = 0.0;
        this.activa = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getVendedorId() { return vendedorId; }
    public void setVendedorId(String vendedorId) { this.vendedorId = vendedorId; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getVendedorNombre() { return vendedorNombre; }
    public void setVendedorNombre(String vendedorNombre) { this.vendedorNombre = vendedorNombre; }

    @Override
    public String toString() {
        return "Nota #" + id.substring(0, 8) + " - $" + String.format("%.2f", total);
    }
}
