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

public class ShowHistory extends AppCompatActivity {

    private static final String sv_url = "192.168.1.44";
    private static final int sv_port = 10000;

    private static final int ID_LOGIN = 0;
    private static final int ID_GETRECOMENDATION = 1;
    private static final int ID_GETSPEACHDATA = 2;
    private static final int ID_GETHISTORY = 7;


    private ListView listView;
    private String id_usuario;
    private ArrayList<CharlaRecomendada> historial;
    private CharlaRecomendada charla;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_history);

        listView = findViewById(R.id.listaHist);
        Intent intent = getIntent();
        id_usuario = (String) intent.getStringExtra("id_usuario");

        listRequest lr = new listRequest();
        lr.execute();

    }

    public void showRecomendations(View v){

        Intent intent = new Intent(ShowHistory.this, ShowRecomendation.class);
        intent.putExtra("id_usuario", id_usuario);
        startActivity(intent);

    }

    private class listRequest extends AsyncTask<String, Void, String> {

        String res = "";

        @Override
        protected String doInBackground(String... params) {

            String request;

            try {

                Socket socket = new Socket(sv_url, sv_port);

                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                request = ID_GETHISTORY + ";" + id_usuario;

                pw.write(request);
                pw.flush();
                socket.shutdownOutput();

                InputStream is = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                String aux;
                String result2 = "";

                while ((aux = in.readLine()) != null) {
                    result2 += aux;
                    //System.out.println("Respuesta del servidor: " + aux);
                }

                res = result2;

                System.out.println("RESULTADO 2: " + result2);
                String[] tmp = result2.split(";");
                String[] infoCharla;
                historial = new ArrayList<>();


                System.out.println("ID_USUARIO : " + id_usuario);

                for (int i = 0; i < tmp.length; i = i + 2) {
                    System.out.println("i= " + i);
                    System.out.println(tmp[i]);
                    System.out.println(tmp[i + 1]);
                    historial.add(new CharlaRecomendada(Integer.parseInt(tmp[i]), tmp[i + 1], 1));
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

    public void setListener() {
        ListAdapter2 listAdapter = new ListAdapter2(this, historial);
        listView.setAdapter(listAdapter);
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                charla = historial.get(i);
                RequestSpeachData r = new RequestSpeachData();
                r.execute();

            }
        });
    }

    private class RequestSpeachData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String request;

            String data = "";

            try {

                Socket socket = new Socket(sv_url, sv_port);

                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                request = ID_GETSPEACHDATA + ";" + charla.getId();

                pw.write(request);
                pw.flush();
                socket.shutdownOutput();

                InputStream is = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                String aux;

                while ((aux = in.readLine()) != null) {
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

            Intent intent = new Intent(ShowHistory.this, Speach.class);
            System.out.println("\n\n\n\n\n\n\n\n" + result + "\n\n\n\n\n\n\n\n");
            String[] data = result.split(";");

            if (data != null) {

                intent.putExtra("id_usuario", id_usuario);
                intent.putExtra("id_charla", Integer.toString(charla.getId()));
                intent.putExtra("titulo", charla.getTitulo());
                intent.putExtra("ponente", data[0]);
                intent.putExtra("evento", data[1]);
                intent.putExtra("duracion", Integer.parseInt(data[2]));
                intent.putExtra("visitas", Integer.parseInt(data[3]));
                intent.putExtra("url", data[4]);
                intent.putExtra("descripcion", data[5]);
                intent.putExtra("meGusta", "1");

            }

            startActivity(intent);
        }
    }
}