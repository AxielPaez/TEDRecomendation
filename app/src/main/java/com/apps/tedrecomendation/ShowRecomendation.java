package com.apps.tedrecomendation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ShowRecomendation extends AppCompatActivity {

    private String sv_url;
    private static final int sv_port = 10000;

    private static final int ID_LOGIN = 0;
    private static final int ID_GETRECOMENDATION = 1;
    private static final int ID_GETSPEACHDATA = 2;

    private ListView listView;
    String id_usuario;
    ArrayList<CharlaRecomendada> recomendacion;

    CharlaRecomendada charla;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recomendation);

        listView = findViewById(R.id.listaReco);
        Intent intent = getIntent();
        id_usuario =(String) intent.getStringExtra("id_usuario");
        sv_url = (String) intent.getStringExtra("sv_url");
        System.out.println("Show Recomendation ID_USUARIO: "+id_usuario);

        listRequest lr = new listRequest();
        lr.execute();

    }

    private class listRequest extends AsyncTask<String, Void, String> {

        String res = "";

        @Override
        protected String doInBackground(String... params) {

            String request;

            try {

                Socket socket = new Socket(sv_url,sv_port);

                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                request = ID_GETRECOMENDATION +";"+id_usuario;

                pw.write(request);
                pw.flush();
                socket.shutdownOutput();

                InputStream is = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                String aux;
                String result2 ="";

                while ((aux = in.readLine()) != null){
                    result2 += aux;
                    //System.out.println("Respuesta del servidor: " + aux);
                }

                res = result2;

                System.out.println("RESULTADO 2: " +result2);
                String [] tmp = result2.split(";");
                String [] infoCharla;
                recomendacion = new ArrayList<>();

                id_usuario = tmp[0];

                System.out.println("ID_USUARIO : " +id_usuario);

                for(int i=1; i<tmp.length; i=i+3){
                    System.out.println("i= "+i);
                    System.out.println(tmp[i]);
                    System.out.println(tmp[i+1]);
                    System.out.println(tmp[i+2]);
                    recomendacion.add(new CharlaRecomendada(Integer.parseInt(tmp[i]),tmp[i+1], Double.parseDouble(tmp[i+2])));
                }


                is.close();
                in.close();


                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
                res = e.toString();
            }

            return res;
        }
        @Override
        protected void onPostExecute(String result) {
            setListener();
        }

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

                request = ID_GETSPEACHDATA +";"+charla.getId();

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

                os.close();
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

                intent.putExtra("id_usuario", id_usuario);
                intent.putExtra("sv_url", sv_url);
                intent.putExtra("backTo", "0");

                intent.putExtra("id_charla", Integer.toString(charla.getId()));
                intent.putExtra("titulo", charla.getTitulo());
                intent.putExtra("ponente", data[0]);
                intent.putExtra("evento", data[1]);
                intent.putExtra("duracion", Integer.parseInt(data[2]));
                intent.putExtra("visitas", Integer.parseInt(data[3]));
                intent.putExtra("url", data[4]);
                intent.putExtra("descripcion", data[5]);
                intent.putExtra("meGusta", "0");

            }

            startActivity(intent);
        }
    }

    public void showHistory(View v){

        Intent intent = new Intent(ShowRecomendation.this, ShowHistory.class);
        intent.putExtra("id_usuario", id_usuario);
        intent.putExtra("sv_url", sv_url);
        startActivity(intent);

    }

    public void setListener(){
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

}