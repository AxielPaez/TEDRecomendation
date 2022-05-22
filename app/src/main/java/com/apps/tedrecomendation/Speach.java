package com.apps.tedrecomendation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

public class Speach extends AppCompatActivity {

    TextView tituloTV;
    TextView ponenteTV;
    TextView eventoTV;
    TextView duracionTV;
    TextView visitasTV;
    TextView urlTV;
    TextView descripcionTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speach);

        tituloTV = (TextView) this.findViewById(R.id.titulo);
        ponenteTV = (TextView) this.findViewById(R.id.ponenteTxt);
        eventoTV = (TextView) this.findViewById(R.id.eventoTxt);
        duracionTV = (TextView) this.findViewById(R.id.duracionTxt);
        visitasTV = (TextView) this.findViewById(R.id.visitasTxt);
        urlTV = (TextView) this.findViewById(R.id.urlTxt);
        descripcionTV = (TextView) this.findViewById(R.id.descripcionTxt);

        Intent intent = getIntent();

        if(intent != null){

            String titulo = intent.getStringExtra("titulo");
            String ponente = intent.getStringExtra("ponente");
            String evento = intent.getStringExtra("evento");
            int duracion = intent.getIntExtra("duracion",-1);
            int visitas = intent.getIntExtra("visitas", -1);
            String url = intent.getStringExtra("url");
            String descripcion = intent.getStringExtra("descripcion");

            int duracionMin = duracion/60;
            int duracionSeg = duracion - (duracionMin*60);

            String duracionSegStr = String.valueOf(duracionSeg);

            if(duracionSegStr.length() == 1){
                duracionSegStr = "0"+duracionSegStr;
            }


            tituloTV.setText(titulo);
            ponenteTV.setText(ponente);
            eventoTV.setText(evento);
            duracionTV.setText(duracionMin+":"+duracionSegStr);
            visitasTV.setText(Integer.toString(visitas));
            urlTV.setText(url);
            descripcionTV.setText(descripcion);

        }
    }
}