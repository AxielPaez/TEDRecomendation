package com.apps.tedrecomendation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
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
import java.io.Reader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

public class RegisterActivity extends AppCompatActivity {

    private static final String sv_url = "192.168.1.44";
    private static final int sv_port = 10000;

    TextView p12certificate2, pemcertificate2;
    EditText userET, p12passwordET;

    Context context;
    Intent intent;
    InputStream is;

    byte auxdatotosign[];

    private static final int ID_LOGIN = 1;
    private static final int ID_GETSPEACHDATA = 2;
    private static final int ID_ALMACENAPEM = 3;

    private static final int UNEXPECTED = -1;
    private static final int NOERROR = 0;
    private static final int P12NOEXIST = 1;
    private static final int PEMNOEXIST = 2;
    private static final int ERRORSIGNATURE = 3;
    private static final int ERRORVERIFY = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userET = (EditText) findViewById(R.id.editTextTextPersonName2);
        p12passwordET = (EditText) findViewById(R.id.editTextTextPersonName3);

        p12certificate2 = (TextView) findViewById(R.id.p12certificate2);
        pemcertificate2 = (TextView) findViewById(R.id.pemcertificate2);

        if (isStoragePermissionGranted()) {
            Toast.makeText(RegisterActivity.this,  "Permission is granted ", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(RegisterActivity.this,  "Permission is granted ", Toast.LENGTH_SHORT).show();
        }

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

                        p12certificate2.setText(PathHolder+"/"+myfichero2);

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
                        Toast.makeText(RegisterActivity.this,  "error is: " + e.toString(), Toast.LENGTH_SHORT).show();
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

                        pemcertificate2.setText(PathHolder+"/"+myfichero2);

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
                        Toast.makeText(RegisterActivity.this,  "error is: " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:

        }
    }

    public void firma(View v) {
        // creo y verifico una firma leyendo un fichero *p12
        Register register= new Register();
        register.execute();
    }

    /**
     * Creo y Verifico una firma digital leyendo un fichero .p12
     */
    class Register extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {

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


            try{

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
                CertificateFactory cf = CertificateFactory.getInstance("X.509");

                pemDir = context.getApplicationContext().getExternalFilesDir("FILES");
                pemFile =new File(pemDir, pemFileName);

                if (!pemFile.exists()) return PEMNOEXIST;

                InputStream caInput = new BufferedInputStream(new FileInputStream(pemFile));
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

                //Si puedo verificar la firma, almaceno el fichero PEM en la BBDD junto con el nombre de usuario
                if(s.verify(signature)) {

                    Socket socket = new Socket(sv_url, sv_port);
                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);

                    StringBuilder textBuilder = new StringBuilder();
                    try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pemFile), Charset.forName(StandardCharsets.UTF_8.name())))) {
                        int c = 0;
                        while ((c = reader.read()) != -1) {
                            textBuilder.append((char) c);
                        }
                    }

                    System.out.println("TEXT BUILDER: "+textBuilder.toString());
                    String request = ID_ALMACENAPEM + ";" + userName + ";" + textBuilder.toString();

                    pw.write(request);
                    pw.flush();
                    socket.shutdownOutput();

                    socket.close();

                    return NOERROR;
                }else{
                    return ERRORVERIFY;
                }

            }catch (Exception e){ e.printStackTrace(); }

            return UNEXPECTED;
        }

        @Override
        protected  void onPostExecute(Integer s) {
            if(s == P12NOEXIST) {
                Toast.makeText(RegisterActivity.this, "P12 no existe !!!!!", Toast.LENGTH_SHORT).show();
            } else if (s == PEMNOEXIST) {
                Toast.makeText(RegisterActivity.this, "Pem no existe !!!!!", Toast.LENGTH_SHORT).show();
            } else if (s == ERRORSIGNATURE) {
                Toast.makeText(RegisterActivity.this, "Error creando signature", Toast.LENGTH_SHORT).show();
            } else if (s == ERRORVERIFY) {
                Toast.makeText(RegisterActivity.this, "Error verificando signature", Toast.LENGTH_SHORT).show();
            } else if (s == UNEXPECTED) {
                Toast.makeText(RegisterActivity.this, "Error inesperado", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(RegisterActivity.this, "Usuario registrado", Toast.LENGTH_SHORT).show();
            }

        }
    }
}