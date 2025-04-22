package com.example.proyectofinaluem;

public class Usuario {
    private String id;
    private String correo;
    private String contrasenia;

    // Constructor vacío necesario para Firebase
    public Usuario() {
    }

    // Constructor completo
    public Usuario(String id, String correo, String contrasenia) {
        this.id = id;
        this.correo = correo;
        this.contrasenia = contrasenia;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
}