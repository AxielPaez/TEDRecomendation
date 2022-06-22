package com.apps.tedrecomendation;

import androidx.annotation.NonNull;
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
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    private String sv_url;
    private static final int sv_users = 5220;

    private static final int ID_LOGIN = 0;

    private static final int UNEXPECTED = -1;
    private static final int CORRECT = 0;
    private static final int P12NOEXIST = 1;
    private static final int ERRORSIGNATURE = 2;
    private static final int USRNOREGIS = 3;
    private static final int ERRORSIGNATURE2 = 4;

    EditText ipET, userET, p12passwordET;

    Intent intent;
    Context context;
    byte[] auxdatotosign;
    InputStream is;

    String Mycipher;
    String IPpeer;
    InetAddress IPpeer_innet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userET = (EditText) findViewById(R.id.editTextTextPersonName);
        p12passwordET = (EditText) findViewById(R.id.editTextTextPassword);
        ipET = (EditText) findViewById(R.id.ipET);


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
                if(cursor != null) cursor.close();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){

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

                sv_url = ipET.getText().toString();

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
                String[] result = new String[10];

                SSLContext ctx;
                KeyManagerFactory kmf, kmf2;
                KeyStore ks1, ks2;

                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                ks1 = KeyStore.getInstance("BKS");
                ctx = SSLContext.getInstance("TLS");

                KeyStore keyStore = null;
                // keyStore = KeyStore.getInstance("JKS");
                keyStore = KeyStore.getInstance("BKS");

                InputStream certificateStream = getResources().openRawResource(R.raw.cacertificado);

                CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
                java.security.cert.Certificate[]  chain = {};
                chain = certificateFactory.generateCertificates(certificateStream).toArray(chain);

                certificateStream.close();



                //Chain es un array de certificados que en el valor 0 tiene nuestro certificado

                System.out.println(  ((X509Certificate)chain[0]).toString());
                System.out.println("\n\n");
                System.out.println("LEIDO CERTIFICADO:");
                System.out.println("\tPROPIETARIO: " + ((X509Certificate)chain[0]).getSubjectDN( ));
                System.out.println("\tEMISOR: " +((X509Certificate)chain[0]).getIssuerDN( ));
                System.out.println("\tVALIDDEZ: " +((X509Certificate)chain[0]).getNotBefore() + " to " + ((X509Certificate)chain[0]).getNotAfter( ));
                System.out.println("\tNUMERO DE SERIE: " + ((X509Certificate)chain[0]).getSerialNumber( ));
                System.out.println("\tALGORITMOS: " +((X509Certificate)chain[0]).getSigAlgName( ));
                certificateStream.close();

                String Alias="oooooo";

                //la clave privada tiene que corresponden con el certificado que enta em el array 0 de chaihn


                keyStore.load(null,null);

                keyStore.setEntry( Alias,   new KeyStore.TrustedCertificateEntry(chain[0]), null);



                // TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());


                tmf.init(keyStore);


                ctx.init(null, tmf.getTrustManagers(), null);


                Socket socket_cliente = new Socket(sv_url, sv_users);

                System.out.println("CREADO SOCKET tcp");

                IPpeer_innet = socket_cliente.getInetAddress();


                System.out.println("CL:Host: " + IPpeer_innet);


                OutputStream Flujo_salida = socket_cliente.getOutputStream();
                InputStream Flujo_entrada = socket_cliente.getInputStream();

                DataOutputStream Flujo_s = new DataOutputStream(Flujo_salida);
                DataInputStream Flujo_e = new DataInputStream(Flujo_entrada);


                String mensaje_inicial;
                boolean ssl;
                boolean ssl_activated=true;

                if (ssl_activated) {
                    mensaje_inicial = "ssl";

                } else {

                    mensaje_inicial = "nada";


                }
                Flujo_s.writeUTF(mensaje_inicial);
                System.out.println("CL: ENVIADO MENSAJE INICIAL");


                SSLSocketFactory sslSf = ctx.getSocketFactory();
                //Actualizamos el socketm para que ahora sea ssl
                SSLSocket sslsocket = (SSLSocket) sslSf.createSocket(socket_cliente, null, socket_cliente.getPort(), false);
                // el modo de usar el soclet debe de ser cliente
                sslsocket.setUseClientMode(true);
                System.out.println("CREADO SOCKET SSL");


                SSLSession sesion = sslsocket.getSession();

                IPpeer = sesion.getPeerHost();

                System.out.println("CL:Host: " + IPpeer);


                Mycipher = sesion.getCipherSuite();
                System.out.println("CL:Cipher is " + Mycipher);


                System.out.println("CLProtocol is " + sesion.getProtocol());

                OutputStream Flujo_salida2 = sslsocket.getOutputStream();
                InputStream Flujo_entrada2 = sslsocket.getInputStream();

                DataOutputStream Flujo_s2 = new DataOutputStream(Flujo_salida2);
                DataInputStream Flujo_e2 = new DataInputStream(Flujo_entrada2);

                FileInputStream fis = new FileInputStream(signatureFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object o = ois.readObject();

                String data2 = (String) o;

                o = ois.readObject();
                byte[] signature = (byte[]) o;

                ois.close();
                fis.close();

                long tamano_req = signature.length;

                System.out.println("Tamano calculado fich req.pem: " + tamano_req + " Bytes");

                //envio longitud fichero req.pem
                System.out.println("envio longitud fichero: " + tamano_req + " Bytes");
                Flujo_s2.writeLong(tamano_req);

                //enviamos fichero
                System.out.println("envio  fichero signature: ");
                Flujo_s2.write(signature);

                int ack = Flujo_e2.readInt();

                if(ack == 1) System.out.println("ACK recibido, confirmada recepcion del fichero signature");
                else System.out.println("Error en la recepcion del fichero signature");

                byte[] userNameByteArray = userName.getBytes(StandardCharsets.UTF_8);

                Flujo_s2.writeInt(userNameByteArray.length);
                int comprobacion = Flujo_e2.readInt();

                System.out.println(userNameByteArray.length);
                System.out.println(comprobacion);
                if(userNameByteArray.length == comprobacion) System.out.println("Longitud Name User correcta");

                System.out.println("Nombre de usuario: "+userName);
                Flujo_s2.write(userNameByteArray);

                int userID = Flujo_e2.readInt();

                if(userID == -1) return USRNOREGIS;
                else if(userID == -2) return ERRORSIGNATURE2;
                else id_usuario = String.valueOf(userID);

                socket_cliente.close();
                sslsocket.close();



                System.out.println("CL:antes del close");



            } catch (Exception e) {
                e.printStackTrace();
                res = UNEXPECTED;
            }

            res = CORRECT;

            return res;
        }

        @Override
        protected void onPostExecute(Integer result) {
            //textView.setText(result);
            if(result == CORRECT) openShowRecomendation(id_usuario);
            else if(result == P12NOEXIST ) Toast.makeText(MainActivity.this, "No se ha seleccionado un fichero P12", Toast.LENGTH_SHORT).show();
            else if(result == ERRORSIGNATURE ) Toast.makeText(MainActivity.this, "Error al crear la firma ", Toast.LENGTH_SHORT).show();
            else if(result == USRNOREGIS ) Toast.makeText(MainActivity.this, "No hay ningún usuario registrado con ese nombre", Toast.LENGTH_SHORT).show();
            else if(result == ERRORSIGNATURE2 ) Toast.makeText(MainActivity.this, "La firma no corresponde con el fichero PEM registrado", Toast.LENGTH_SHORT).show();
            else if(result == UNEXPECTED) Toast.makeText(MainActivity.this, "Ha saltado excepcion", Toast.LENGTH_SHORT).show();
        }
    }

    public void register(View v){

        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);

    }


}