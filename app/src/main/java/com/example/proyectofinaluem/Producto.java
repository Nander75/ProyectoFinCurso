package com.example.proyectofinaluem;

import java.io.Serializable;

public class Producto implements Serializable {

    private String id;
    private String nombre;
    private String marca;
    private String talla;
    private int cantidad;
    private double precio;
    private String imagen; // (por ahora vacío)

    public Producto() {
        // Constructor vacío necesario para Firebase
    }

    public Producto(String id, String nombre, String marca, String talla, int cantidad, double precio, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.talla = talla;
        this.cantidad = cantidad;
        this.precio = precio;
        this.imagen = imagen;
    }

    // Getters y Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}
