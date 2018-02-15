package com.example.android.mufflefurnace;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout settings = (LinearLayout) findViewById(R.id.menu_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });

        LinearLayout programs = (LinearLayout) findViewById(R.id.menu_programs);
        programs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ProgramsActivity.class);
                startActivity(i);
            }
        });

        LinearLayout connect = (LinearLayout) findViewById(R.id.menu_connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                //intent.putExtra("only_access_points", true);
                intent.putExtra("extra_prefs_show_button_bar", true);
                //intent.putExtra("wifi_enable_next_on_connect", true);
                startActivityForResult(intent, 1);



/*
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings" );
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.putExtra("extra_prefs_show_button_bar", true);

                startActivity( intent);
*/


               // startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                //Intent i = new Intent(MainActivity.this, ConnectActivity.class);
                //startActivity(i);
            }
        });
    }
}
