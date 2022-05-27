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
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private static final int ID_LOGIN = 0;

    TextView textView, p12certificate, pemcertificate;
    EditText userET, p12passwordET;

    private static final int UNEXPECTED = -1;
    private static final int NOERROR = 0;
    private static final int P12NOEXIST = 1;
    private static final int PEMNOEXIST = 2;
    private static final int ERRORSIGNATURE = 3;
    private static final int ERRORVERIFY = 4;

    Intent intent;
    Context context;
    byte auxdatotosign[];
    InputStream is;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userET = (EditText) findViewById(R.id.editTextTextPersonName);
        p12passwordET = (EditText) findViewById(R.id.editTextTextPassword);

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

    public void openShowRecomendation(String id_usuario){
        Intent intent = new Intent(this, ShowRecomendation.class);
        intent.putExtra("id_usuario", id_usuario);
        startActivity(intent);
    }

    public void login(View v){
        Login login = new Login();
        login.execute();
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



    /**
     * Me conecto a la Base de Datos MySQL
     * Compruebo si el usuario y la contraseña son válidos
     * Si usuario y contraseña son validos, obtengo las recomendaciones correspondientes
     */
    private class Login extends AsyncTask<String, Void, Integer> {

        int res;
        String id_usuario;
        ArrayList<CharlaRecomendada> charlas;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Toast.makeText(MainActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected Integer doInBackground(String... params) {

            KeyStore ks;
            char[] clave;

            String userName;
            String p12password;

            String p12FileName = "cert.p12";
            String pemFileName = "cert.pem";
            String signatureFileName = "signature";

            File p12File;
            File pemFile;
            File signatureFile;

            File p12Dir;
            File pemDir;
            File signatureDir;

            try {

                userName = userET.getText().toString();
                p12password = p12passwordET.getText().toString();

                ks = KeyStore.getInstance("PKCS12");

                p12Dir = context.getApplicationContext().getExternalFilesDir("FILES");
                p12File = new File(p12Dir, p12FileName);

                if(!p12File.exists()) return P12NOEXIST;

                System.out.println("Reading *.P12 file");
                InputStream fichp12= new FileInputStream(p12File);
                clave = p12password.toCharArray();
                ks.load(fichp12, clave);


                int n = 1;
                String alias = null;
                Enumeration<String> enumeration = ks.aliases();
                while (enumeration.hasMoreElements()) {
                    alias = (String) enumeration.nextElement();
                    n = n + 1;
                }

                fichp12.close();

                Key key = ks.getKey(alias, clave);

                signatureDir = context.getExternalFilesDir("Signatures");
                signatureFile= new File(signatureDir, signatureFileName);

                FileOutputStream fos = new FileOutputStream(signatureFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                if (!signatureFile.exists()) return ERRORSIGNATURE;

                // esto hay que hacerlo en androi10
                Signature sig = Signature.getInstance("SHA512withRSA");

                sig.initSign((PrivateKey) key);

                byte[] buf = userName.getBytes();
                sig.update(buf);
                oos.writeObject(userName);
                auxdatotosign =  sig.sign();

                oos.writeObject(auxdatotosign);
                oos.close();

                /*
                Verifico que la clave publica es la correspondiente a la clave privada
                 */
                Socket socket = new Socket(sv_url,sv_port);
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                String request = ID_LOGIN +";"+userName;

                pw.write(request);
                pw.flush();
                socket.shutdownOutput();

                InputStream is = socket.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF8"));

                String aux;
                String result2 ="";

                while ((aux = in.readLine()) != null){
                    result2 += aux +"\n";
                    System.out.println("aux: "+aux);
                    //System.out.println("Respuesta del servidor: " + aux);
                }

                String[] data = result2.split(";");
                id_usuario = data[0];
                String pem = data[1];

                System.out.println("Main Activity ID_USUARIO: "+id_usuario);
                System.out.println("Main Activity PEM: "+pem);



                CertificateFactory cf = CertificateFactory.getInstance("X.509");

                try{
                    InputStream caInput = new BufferedInputStream(new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)));
                    Certificate cert = cf.generateCertificate(caInput);
                    System.out.println(cert.toString());

                    PublicKey pk= cert.getPublicKey();

                    FileInputStream fis = new FileInputStream(signatureFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    Object o = ois.readObject();

                    String data2 = (String) o;

                    o = ois.readObject();
                    byte[] signature = (byte[]) o;

                    ois.close();
                    fis.close();

                    Signature s = Signature.getInstance("SHA512withRSA");
                    s.initVerify(pk);
                    s.update(data2.getBytes());

                    if(s.verify(signature)) return 1;
                    else return 0;

                } catch (IOException e) {
                }

            } catch (Exception e) {
                e.printStackTrace();
                res = UNEXPECTED;
            }

            return res;
        }

        @Override
        protected void onPostExecute(Integer result) {
            //textView.setText(result);
            if(result == 1 ) openShowRecomendation(id_usuario);
            else if(result == 0 ) Toast.makeText(MainActivity.this, "Datos incorrectos", Toast.LENGTH_SHORT).show();
            else if(result == -1) Toast.makeText(MainActivity.this, "Ha saltado excepcion", Toast.LENGTH_SHORT).show();
        }
    }

    public void register(View v){

        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);

    }


}