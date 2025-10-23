package com.example.myapplication;



public class Reseña {
    private String pacienteNombre;
    private int calificacion;
    private String comentario;

    public Reseña(String pacienteNombre, int calificacion, String comentario) {
        this.pacienteNombre = pacienteNombre;
        this.calificacion = calificacion;
        this.comentario = comentario;
    }

    public String getPacienteNombre() {
        return pacienteNombre;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public String getComentario() {
        return comentario;
    }
}
