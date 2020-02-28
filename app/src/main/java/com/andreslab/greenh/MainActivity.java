package com.andreslab.greenh;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    static final int CONFIG_REQUEST = 1;  // The request code

    private static final int MY_PERMISSION_REQUEST_RECEIVE_SMS = 0;

    TextView txt_command;
    public static final String OTP_REGEX = "[0-9]{1,6}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_command = (TextView) findViewById(R.id.txt_command);


        SharedPreferences pref = getApplicationContext().getSharedPreferences("config", 0); // 0 - for private mode
        String host = pref.getString("host", null);

        if (host == null) {
            Intent i = new Intent(MainActivity.this, ConfigActivity.class);
            startActivityForResult(i, CONFIG_REQUEST);
        }

        //if permission is not granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)){
                //user denied permission
                Toast.makeText(this, "PERMISOS DENEGADO: SMSRECEVICE", Toast.LENGTH_SHORT).show();
            }else{
                //pop up asking permission
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECEIVE_SMS}, MY_PERMISSION_REQUEST_RECEIVE_SMS);
            }
            
        }
    }

    void receiveSMSListener(){
        //reception msg
        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String messageText) {

                //From the received text string you may do string operations to get the required OTP
                //It depends on your SMS format
                Log.e("Message",messageText);
                Toast.makeText(MainActivity.this,"Message: "+messageText,Toast.LENGTH_LONG).show();

                // If your OTP is six digits number, you may use the below code

                Pattern pattern = Pattern.compile(OTP_REGEX);
                Matcher matcher = pattern.matcher(messageText);
                String otp = "";
                while (matcher.find())
                {
                    otp = matcher.group();
                }

                Toast.makeText(MainActivity.this,"OTP: "+ otp ,Toast.LENGTH_LONG).show();

                sendCommand();

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case MY_PERMISSION_REQUEST_RECEIVE_SMS:
            {
                if(grantResults.length>0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Gracias por los permisos", Toast.LENGTH_SHORT).show();
                    receiveSMSListener();
                }else{
                    Toast.makeText(this, "Se necesitan permisos", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONFIG_REQUEST) {
            if (resultCode == RESULT_OK){
                Log.i("resultado","configuracion terminado");
            }
        }

    }

    void sendCommand() {

        RequestQueue queue = Volley.newRequestQueue(this);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("config", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        String host = pref.getString("host", null);
        String extension = pref.getString("extension", null);

        String url = host.concat(extension);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("command", "run");

                return params;
            }
        };
        queue.add(postRequest);
    }
}
