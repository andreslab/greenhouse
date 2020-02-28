package com.andreslab.greenh;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigActivity extends AppCompatActivity {

    Button btn_save_config;
    EditText txt_host;
    EditText txt_ext;
    RadioGroup rg;
    Switch switch_on;

    enum STATUS {
        DEV, PRE, PRO
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        btn_save_config = (Button) findViewById(R.id.btn_save_config);
        txt_host = (EditText) findViewById(R.id.txt_host);
        txt_ext = (EditText) findViewById(R.id.txt_ext);
        rg = (RadioGroup) findViewById(R.id.status_select);
        switch_on = (Switch) findViewById(R.id.is_on);


        btn_save_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String host = txt_host.getText().toString();
                String extension = txt_ext.getText().toString();

                String status;

                switch (rg.getCheckedRadioButtonId()){
                    case R.id.rb1:
                        status = STATUS.DEV.name();
                        break;
                    case R.id.rb2:
                        status = STATUS.PRE.name();
                        break;
                    case R.id.rb3:
                        status = STATUS.PRO.name();
                        break;
                    default:
                        status = STATUS.DEV.name();
                }

                boolean is_on = switch_on.getKeepScreenOn();

                if(validateData(host)){
                    boolean saveOk = saveConfig(host, extension, status, is_on);

                    if(saveOk) {
                        Intent result = new Intent(ConfigActivity.this, MainActivity.class);
                        setResult(Activity.RESULT_OK, result);
                        finish();
                    }
                }

            }
        });
    }

    boolean validateData(String host){

        //validate ip
        Pattern p = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        Matcher m = p.matcher(host);
        boolean resultado = m.find();

        if(resultado){
            return true;
        }
        return false;
    }

    boolean saveConfig(String host, String extension, String status, boolean is_on ){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("config", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        String _extension = extension;

        if((extension.charAt(0)) != new Character('/')){
            _extension = "/".concat(extension);
        }

        editor.putString("host", "http://".concat(host));// host: 192.168.0.1
        editor.putString("extension",_extension);
        editor.putString("status", status);
        editor.putBoolean("is_on", is_on); //save data

        editor.commit(); // commit changes

        return true;
    }
}
