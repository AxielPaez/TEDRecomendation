package com.apps.tedrecomendation;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CharlaRecomendada implements Serializable {

    private final int id;
    private final String titulo;
    private final double puntuacion;

    public CharlaRecomendada(int id, String titulo, double puntuacion){
        this.id = id;
        this. titulo = titulo;
        this.puntuacion = puntuacion;
    }

    public int getId(){
        return id;
    }

    public String getTitulo(){
        return titulo;
    }

    public double getPuntuacion() {
        return puntuacion;
    }

    @NonNull
    public String toString(){
        return  "Titulo: " + titulo + " Puntuacion: " + puntuacion;
    }

}
