package com.apps.tedrecomendation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Speach extends AppCompatActivity {

    TextView tituloTV;
    TextView ponenteTV;
    TextView eventoTV;
    TextView duracionTV;
    TextView visitasTV;
    TextView urlTV;
    TextView descripcionTV;

    CheckBox meGusta;

    private static final int ID_ADDUSUARIOCHARLA = 5;
    private static final int ID_DELUSUARIOCHARLA = 6;

    private static final String sv_url = "192.168.1.44";
    private static final int sv_port = 10000;

    String id_usuario;
    String id_charla;


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
        meGusta = (CheckBox) this.findViewById(R.id.meGusta);

        Intent intent = getIntent();

        if(intent != null){

            id_usuario = intent.getStringExtra("id_usuario");
            id_charla = intent.getStringExtra("id_charla");
            String titulo = intent.getStringExtra("titulo");
            String ponente = intent.getStringExtra("ponente");
            String evento = intent.getStringExtra("evento");
            int duracion = intent.getIntExtra("duracion",-1);
            int visitas = intent.getIntExtra("visitas", -1);
            String url = intent.getStringExtra("url");
            String descripcion = intent.getStringExtra("descripcion");
            String mg = intent.getStringExtra("meGusta");

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

            if(mg.equals("1")){
                meGusta.setChecked(true);
            }else{
                meGusta.setChecked(false);
            }

        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = meGusta.isChecked();

        if(checked){
            System.out.println("ME GUSTA");
            AddUsuarioCharla auc = new AddUsuarioCharla();
            auc.execute();
        }else{
            System.out.println("NO ME GUSTA");
            DelUsuarioCharla duc = new DelUsuarioCharla();
            duc.execute();
        }
    }

    private class AddUsuarioCharla extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String request;
            String data ="";
            try {

                Socket socket = new Socket(sv_url,sv_port);
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);
                request = ID_ADDUSUARIOCHARLA +";"+id_usuario+";"+id_charla;
                pw.write(request);
                pw.flush();
                socket.shutdownOutput();
                os.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }
    }
    private class DelUsuarioCharla extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String request;
            String data ="";
            try {

                Socket socket = new Socket(sv_url,sv_port);
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);
                request = ID_DELUSUARIOCHARLA +";"+id_usuario+";"+id_charla;
                pw.write(request);
                pw.flush();
                socket.shutdownOutput();
                os.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }
    }
}