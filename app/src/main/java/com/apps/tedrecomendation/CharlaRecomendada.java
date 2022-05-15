package com.apps.tedrecomendation;

public class CharlaRecomendada {

    private final double id;
    private final double puntuacion;

    public CharlaRecomendada(double id, double puntuacion){
        this. id = id;
        this.puntuacion = puntuacion;
    }

    public double getId(){
        return id;
    }

    public double getPuntuacion() {
        return puntuacion;
    }

    public String toString(){
        return  "ID: " + String.valueOf(id) + " Puntuacion: " + String.valueOf(puntuacion);
    }

}
