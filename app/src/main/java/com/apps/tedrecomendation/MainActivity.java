package com.apps.tedrecomendation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String db_url = "jdbc:mysql://192.168.1.41:3306/TED?characterEncoding=utf8";
    private static final String db_user = "dba";
    private static final String db_pass = "dba";

    private static final String sv_url = "192.168.1.41";
    private static final int sv_port = 10000;

    Button button;
    TextView textView;
    EditText user, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) this.findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        user = (EditText) findViewById(R.id.editTextTextPersonName);
        password = (EditText) findViewById(R.id.editTextTextPassword);

    }

    public void select(View v){

        ConnectMySql connectMySql = new ConnectMySql();
        connectMySql.execute("");

    }

    public void openShowRecomendation(ArrayList<CharlaRecomendada> charlas){
        Intent intent = new Intent(this, ShowRecomendation.class);

        intent.putExtra("recomendacion", charlas);
        startActivity(intent);
    }

    private class ConnectMySql extends AsyncTask<String, Void, String> {

        String res = "";
        ArrayList<CharlaRecomendada> charlas;
        //CharlaRecomendada[] charlas;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Please wait...", Toast.LENGTH_SHORT)
                    .show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                String sql = "select id from usuario where nombre = ? and correo = ?";
                String result;

                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(db_url, db_user, db_pass);

                PreparedStatement pst=con.prepareStatement(sql);
                pst.setString(1,user.getText().toString());
                pst.setString(2,password.getText().toString());

                ResultSet rs = pst.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();

                if(!rs.next()){
                    result = "Usuario o contraseña incorrectos";
                }else{
                    result = rs.getString(1) + "\n";
                }

                Socket socket = new Socket(sv_url,sv_port);

                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                pw.write(result);
                pw.flush();
                socket.shutdownOutput();

                InputStream is = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                String aux = null;
                String result2 ="";

                while ((aux = in.readLine()) != null){
                    result2 += aux;
                    //System.out.println("Respuesta del servidor: " + aux);
                }

                res = result2;

                String [] tmp = result2.split(",");
                charlas = new ArrayList<CharlaRecomendada>();

                for(int i=0; i<tmp.length; i=i+2){
                    charlas.add(new CharlaRecomendada(Double.parseDouble(tmp[i]), Double.parseDouble(tmp[i+1])));
                    System.out.println(charlas.get(1/2).toString());
                }

                System.out.println("###########################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################");
                System.out.println("Recomend:");
                System.out.println(charlas.get(0).toString());


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
            textView.setText(result);

            openShowRecomendation(charlas);
        }
    }
}