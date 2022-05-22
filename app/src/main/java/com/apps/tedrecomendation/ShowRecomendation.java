package com.apps.tedrecomendation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ShowRecomendation extends AppCompatActivity {

    private static final String sv_url = "192.168.1.44";
    private static final int sv_port = 10000;

    private static final int ID_LOGIN = 1;
    private static final int ID_GETSPEACHDATA = 2;

    private ListView listView;
    List<CharlaRecomendada> tmp = new ArrayList<CharlaRecomendada>();
    ArrayList<CharlaRecomendada> recomendacion;

    CharlaRecomendada charla;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recomendation);

        System.out.println("###########################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################");

        listView = findViewById(R.id.listaReco);
        Intent intent = getIntent();
        recomendacion = (ArrayList<CharlaRecomendada>) intent.getSerializableExtra("recomendacion");

        ListAdapter listAdapter = new ListAdapter(this, recomendacion);
        listView.setAdapter(listAdapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                charla = recomendacion.get(i);
                RequestSpeachData r = new RequestSpeachData();
                r.execute("");

            }
        });


        }
    private class RequestSpeachData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String request;

            String data ="";

            try {

                Socket socket = new Socket(sv_url,sv_port);

                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                request = ID_GETSPEACHDATA +","+charla.getId();

                pw.write(request);
                pw.flush();
                socket.shutdownOutput();

                InputStream is = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                String aux;

                while ((aux = in.readLine()) != null){
                    data += aux;
                    //System.out.println("Respuesta del servidor: " + aux);
                }

                is.close();
                in.close();

                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {

            Intent intent = new Intent(ShowRecomendation.this, Speach.class);
            System.out.println("\n\n\n\n\n\n\n\n"+result+"\n\n\n\n\n\n\n\n");
            String[] data = result.split(";");

            if(data != null) {


                intent.putExtra("titulo", charla.getTitulo());
                intent.putExtra("ponente", data[0]);
                intent.putExtra("evento", data[1]);
                intent.putExtra("duracion", Integer.parseInt(data[2]));
                intent.putExtra("visitas", Integer.parseInt(data[3]));
                intent.putExtra("url", data[4]);
                intent.putExtra("descripcion", data[5]);

            }

            startActivity(intent);
        }
    }

}