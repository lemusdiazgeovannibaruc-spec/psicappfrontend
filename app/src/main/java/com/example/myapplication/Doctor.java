package com.example.myapplication;

public class Doctor {
    private int id;
    private String nombre;
    private String especialidad;
    private String ubicacion;

    public Doctor(int id, String nombre, String especialidad, String ubicacion) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.ubicacion = ubicacion;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }
}
