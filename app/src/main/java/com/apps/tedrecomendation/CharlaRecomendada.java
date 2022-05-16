package com.apps.tedrecomendation;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CharlaRecomendada implements Serializable {

    private final int id;
    private final double puntuacion;

    public CharlaRecomendada(int id, double puntuacion){
        this. id = id;
        this.puntuacion = puntuacion;
    }

    public int getId(){
        return id;
    }

    public double getPuntuacion() {
        return puntuacion;
    }

    @NonNull
    public String toString(){
        return  "ID: " + id + " Puntuacion: " + puntuacion;
    }

}
