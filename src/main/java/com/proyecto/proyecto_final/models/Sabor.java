package com.heladeria.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sabor {
    @Expose private String id;
    @Expose private String nombre;
    @Expose private String descripcion;
    @Expose private Double precio;
    @Expose private Integer stock;
    @Expose private Boolean activo;

    public Sabor() {}

    public Sabor(String nombre, String descripcion, Double precio, Integer stock) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.activo = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre + " - $" + String.format("%.2f", precio);
    }
}
