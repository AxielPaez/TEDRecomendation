package com.apps.tedrecomendation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private static final String sv_url = "192.168.1.44";
    private static final int sv_port = 10000;

    private static final int ID_LOGIN = 1;

    Button button;
    TextView textView, p12certificate, pemcertificate;
    EditText user, password;

    Intent intent;
    Context context;
    byte auxdatotosign[];
    InputStream is;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) this.findViewById(R.id.textView);
        button = (Button) findViewById(R.id.bt_logIn);
        user = (EditText) findViewById(R.id.editTextTextPersonName);
        password = (EditText) findViewById(R.id.editTextTextPassword);

        p12certificate = (TextView) findViewById(R.id.p12certificate);
        pemcertificate = (TextView) findViewById(R.id.pemcertificate);

        if (isStoragePermissionGranted()) {
            Toast.makeText(MainActivity.this,  "Permission is granted ", Toast.LENGTH_SHORT).show();
        }

    }

    public boolean isStoragePermissionGranted(){

        if(Build.VERSION.SDK_INT >= 23){

            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                System.out.println("Acceso a archivos permitido");
                return true;
            }else{
                System.out.println("Acceso a archivos NO permitido");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }

        }else{
            System.out.println("Acceso a archivos permitido");
            return true;
        }
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {

        String result = null;
        if (uri.getScheme().equals("content")) {

            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            //Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            System.out.println("Permission: "+permissions[0]+ "was "+grantResults[0]);
            Toast.makeText(MainActivity.this,  "Permission is granted ", Toast.LENGTH_SHORT).show();
        }

    }

    public void openShowRecomendation(ArrayList<CharlaRecomendada> charlas){
        Intent intent = new Intent(this, ShowRecomendation.class);

        intent.putExtra("recomendacion", charlas);
        startActivity(intent);
    }

    public void select(View v){
        ConnectMySql connectMySql = new ConnectMySql();
        connectMySql.execute("");
    }

    public void getp12(View v){

        intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 7);

        context = getApplicationContext();

        System.out.println("Saliendo de Getp12" );
    }

    public void getpem(View v) {

        intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 8);

        context = getApplicationContext();

        System.out.println("Saliendo de Getpem" );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            //
            case 7:
                if(resultCode == RESULT_OK){
                    try{
                        System.out.println("Entro resulcode 7" );

                        String PathHolder = data.getData().getPath();
                        System.out.println("PathHolder: " + PathHolder);

                        Uri fileUri = data.getData();

                        String myfichero2 = getFileName(fileUri);

                        ContentResolver cr = context.getContentResolver();
                        System.out.println(" cr :" + cr);

                        is = (InputStream) cr.openInputStream(fileUri);

                        System.out.println(" entro por el try del is creado :");
                        System.out.println(" is creado :");


                        ByteArrayOutputStream bos = null;
                        byte[] buffer = new byte[1024];
                        bos = new ByteArrayOutputStream();

                        int aux = 0;
                        for (int len; (len = is.read(buffer)) != -1; ) {
                            bos.write(buffer, 0, len);

                            aux = aux + len;

                        }

                        byte[] bufferfich = bos.toByteArray();
                        int longitud1 = bufferfich.length;
                        System.out.println("Longitud bufferfich: " + longitud1);


                        // escribimos el fichero en claro  en CLEAR_FILES")
                        File dir = context.getExternalFilesDir("FILES");
                        FileOutputStream outputStream = new FileOutputStream(new File(dir, myfichero2));
                        outputStream.write(bufferfich);
                        outputStream.close();

                        System.out.println("Escrito fichero cert.p12 en directorio app" );
                    }catch (Exception e) {
                        Toast.makeText(MainActivity.this,  "error is: " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case 8:
                if (resultCode == RESULT_OK) {
                    try {
                        String PathHolder = data.getData().getPath();
                        System.out.println("PathHolder: " + PathHolder);


                        Uri fileUri = data.getData();
                        String myfichero2 = getFileName(fileUri);

                        ContentResolver cr = context.getContentResolver();
                        System.out.println(" cr :" + cr);

                        is = (InputStream) cr.openInputStream(fileUri);

                        System.out.println(" entro por el try del is creado :");
                        System.out.println(" is creado :");

                        ByteArrayOutputStream bos = null;
                        byte[] buffer = new byte[1024];
                        bos = new ByteArrayOutputStream();

                        int aux = 0;
                        for (int len; (len = is.read(buffer)) != -1; ) {
                            bos.write(buffer, 0, len);

                            aux = aux + len;

                        }

                        byte[] bufferfich = bos.toByteArray();
                        int longitud1 = bufferfich.length;
                        System.out.println("Longitud bufferfich: " + longitud1);

                        // escribimos el fichero en claro  en CLEAR_FILES")
                        File dir = context.getExternalFilesDir("FILES");
                        FileOutputStream outputStream = new FileOutputStream(new File(dir, myfichero2));
                        outputStream.write(bufferfich);
                        outputStream.close();

                        System.out.println("Escrito fichero cert.pem en directorio app");

                    }catch (Exception e) {
                        Toast.makeText(MainActivity.this,  "error is: " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:

        }
    }

    public void firma(View v) {
        // creo y verifico una firma leyendo un fichero *p12
        SignVerificationTask  signVerificationTask= new SignVerificationTask();
        signVerificationTask.execute();
    }

    /**
     * Creo y Verifico una firma digital leyendo un fichero .p12
     */
    class SignVerificationTask extends  AsyncTask<Void, Void, String[]>{

        @Override
        protected String[] doInBackground(Void... voids) {

            String[] result = new String[10];
            KeyStore ks, ks2;

            try{

                InputStream inputStream = System.in;
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);


                EditText editTextTextPersonName = findViewById(R.id.editTextTextPersonName);
                String data = editTextTextPersonName.getText().toString();

                EditText editTextTextPassword = findViewById(R.id.editTextTextPassword);
                String password = editTextTextPassword.getText().toString();

                char[] clave = password.toCharArray();
                ks = KeyStore.getInstance("PKCS12");

                File dir1 = context.getApplicationContext().getExternalFilesDir("FILES");
                File fs = new File(dir1, "cert.p12");

                p12certificate.setText(fs.getPath());

                if(!fs.exists()){
                    System.out.println("cert.p12 no existe");
                    result[0]="Error intentando firmar !!!!!";
                    result[2]="Fichero *.p12 no existe";
                    result[3]=" ";
                    return result;
                }

                System.out.println("Reading *.P12 file");
                InputStream fichp12= new FileInputStream(fs);
                ks.load(fichp12, clave);
                Enumeration<String> enumeration = ks.aliases();

                String alias = null;
                int n = 1;
                while (enumeration.hasMoreElements()) {
                    System.out.println("\n\n");
                    alias = (String) enumeration.nextElement();
                    System.out.println("alias name " + n + ":  " + alias);


                    X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);
                    System.out.println(certificate.toString());
                    System.out.println("\n\n");
                    n = n + 1;
                }

                fichp12.close();

                System.out.println("salgo de leer el keystore");
                System.out.println("Nombre del alias:  " + alias);

                Key key = ks.getKey(alias, clave);
                PrivateKey privKey = (PrivateKey) key;

                System.out.println("Getting the private key... : "+privKey );

                File dir2 = context.getExternalFilesDir("Signatures");
                String myfichero = "signature";

                File fs1= new File(dir2, myfichero);

                FileOutputStream fos = new FileOutputStream(fs1);
                ObjectOutputStream oos = new ObjectOutputStream(fos);


                if (!fs1.exists())
                {
                    System.out.println("error generating signature file");
                    result[0]="Error while trying to sign/verify !!!!!";
                    result[3]="Error generating signature file";
                    result[2]=" ";
                    return result;
                }

                // esto hay que hacerlo en androi10
                Signature sig = Signature.getInstance("SHA512withRSA");

                sig.initSign((PrivateKey) key);

                byte buf[] = data.getBytes();
                sig.update(buf);
                oos.writeObject(data);
                auxdatotosign =  sig.sign();

                oos.writeObject(auxdatotosign);
                oos.close();

                System.out.println(" Digital signature  OK !!!!!");

                String Strsalida1 = "Digital signature  OK !!!!!";

                result[0]=Strsalida1;
                result[4]=data;

                String data2 = null;
                byte signature[] = null;

            }catch (Exception e){
                e.printStackTrace();

            }

            return result;
        }

        @Override
        protected  void onPostExecute(String[] s){
            if(s[0].equals("Error intentando firmar !!!!!")) {
                Toast.makeText(MainActivity.this, "Error intentando firmar !!!!!", Toast.LENGTH_SHORT).show();

                if (s[2].equals("Fichero *.p12 no existe")) {
                    Toast.makeText(MainActivity.this, "Fichero *.p12 no existe", Toast.LENGTH_SHORT).show();
                } else if (s[3].equals("Error generating signature file")) {
                    Toast.makeText(MainActivity.this, "Error generating signature file", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(MainActivity.this, s[0], Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "Data Signed: "+s[4], Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void VerificaFirma() {
        VerificationTask  verificationTask= new VerificationTask();
        verificationTask.execute();
    }

    class VerificationTask extends AsyncTask<Void, Void, String[]> {
        //Background task which serve for the client
        @Override
        protected String[] doInBackground(Void... params) {

            String[] result = new String[10];
            KeyStore ks, ks2;
            result[3]="";

            try {

                System.out.println("Reading certificate file..:");

                CertificateFactory cf = CertificateFactory.getInstance("X.509");

                File dir1 = context.getApplicationContext().getExternalFilesDir("FILES");
                File fs =new File(dir1, "cert.pem");

                if (!fs.exists()){
                    System.out.println("cert.pem file not exits");
                    result[0]="Error while trying to verify !!!!!";
                    result[2]="*.pem file not exits";
                    result[3]=" ";
                    return result;
                }

                InputStream caInput = new BufferedInputStream(new FileInputStream(fs));

                Certificate cert = cf.generateCertificate(caInput);
                System.out.println(cert.toString());
                System.out.println("Certificate:");
                System.out.println("\tSubject: " + ((X509Certificate)cert).getSubjectDN( ));
                System.out.println("\tissuer: " +((X509Certificate)cert).getIssuerDN( ));
                System.out.println("\tDate: " +((X509Certificate)cert).getNotBefore() + " to " + ((X509Certificate)cert).getNotAfter( ));
                System.out.println("\tSerial: " + ((X509Certificate)cert).getSerialNumber( ));
                System.out.println("\tALGORITMOS: " +((X509Certificate)cert).getSigAlgName( ));

                PublicKey pk= cert.getPublicKey();
                System.out.println("Reading signature file..:");

                String myfichero ="signature";

                File dir2 = context.getExternalFilesDir("Signatures");

                File fs3= new File(dir2, myfichero);
                if (!fs3.exists())

                {
                    System.out.println("signature file not exits");
                    result[0]="Error while trying to verify !!!!!";
                    result[2]=" ";
                    result[3]="signature file not exits";
                    return result;
                }

                FileInputStream fis = new FileInputStream(fs3);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object o = ois.readObject();

                String data2 = null;
                byte signature[] = null;

                data2 = (String) o;

                o = ois.readObject();

                signature = (byte[]) o;

                ois.close();
                fis.close();

                System.out.print("Data to be verified...: ");
                System.out.println(data2);
                System.out.println("\n\n");

                String Salida3 =  data2;
                result[1]=Salida3;
                Signature s = Signature.getInstance("SHA512withRSA");

                s.initVerify(pk);
                s.update(data2.getBytes());

                if (s.verify(signature)) {
                    System.out.println("Verified signature !!!!!");
                    //String Strsalida2 = "Verified signature!!!!!";
                    String Strsalida2 = "Verified signature!! "+"\n"+"Data: "+Salida3;
                    result[0]="1";
                    result[2]=Strsalida2;

                } else {
                    System.out.println("FIRMA NO VERIFICADA !!!!!");
                    String Strsalida3 = "FIRMA NO VERIFICADA !!!!!";

                    result[0]="2";
                    result[2]=Strsalida3;
                }

            }

            catch (Exception e) {
                e.printStackTrace();

                System.out.println("ERROR AL INTENTAR FIRMAR VERIFICAR !!!!!");
                String Strsalida5 = "Error while trying to verify !!!!!";
                result[1]=Strsalida5;

            }
            return result;
        }


        @Override
        protected void onPostExecute(String[] s) {

            if (s[0].equals("Error while trying to verify !!!!!")) {

                Toast.makeText(MainActivity.this,  s[1], Toast.LENGTH_SHORT).show();

                if (s[2].equals("*.pem file not exits")) {
                    Toast.makeText(MainActivity.this,  "*.pem file not exits", Toast.LENGTH_SHORT).show();
                }

                if (s[3].equals("signature file not exits")) {
                    Toast.makeText(MainActivity.this,  "Signature file not exits", Toast.LENGTH_SHORT).show();
                }
            }

            else if (s[0].equals("1")){
                Toast.makeText(MainActivity.this,  s[2], Toast.LENGTH_SHORT).show();

                EditText editText6 = findViewById(R.id.editTextTextPersonName);
                editText6.setText(s[1]);

            }

            else if (s[0].equals("2")){
                Toast.makeText(MainActivity.this,  s[2], Toast.LENGTH_SHORT).show();
                EditText editText6 = findViewById(R.id.editTextTextPassword);
                editText6.setText(s[1]);
            }
        }
    }

    /**
     * Me conecto a la Base de Datos MySQL
     * Compruebo si el usuario y la contraseña son válidos
     * Si usuario y contraseña son validos, obtengo las recomendaciones correspondientes
     */
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

            String request;

            try {

                String nombre = user.getText().toString();
                String contrasena = password.getText().toString();

                Socket socket = new Socket(sv_url,sv_port);

                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                request = ID_LOGIN +","+nombre+","+contrasena;

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
                charlas = new ArrayList<>();

                for(int i=0; i<tmp.length; i=i+3){
                    System.out.println("i= "+i);
                    System.out.println(tmp[i]);
                    System.out.println(tmp[i+1]);
                    System.out.println(tmp[i+2]);
                    charlas.add(new CharlaRecomendada(Integer.parseInt(tmp[i]),tmp[i+1], Double.parseDouble(tmp[i+2])));
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
            textView.setText(result);

            openShowRecomendation(charlas);
        }
    }


}