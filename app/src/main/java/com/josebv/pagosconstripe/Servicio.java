package com.josebv.pagosconstripe;

public class Servicio {

    private String id;
    private String imagen;
    private String titulo;
    private String descripcion;
    private String precio;

    public Servicio(String id, String imagen, String titulo, String descripcion, String precio) {
        this.id = id;
        this.imagen = imagen;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public String getId() {
        return id;
    }

    public String getImagen() {
        return imagen;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPrecio() {
        return precio;
    }

}
