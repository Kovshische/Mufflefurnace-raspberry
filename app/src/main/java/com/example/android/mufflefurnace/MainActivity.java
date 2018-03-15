package com.example.android.mufflefurnace;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static double sensorTemp;
    private final Handler handler = new Handler();
    Runnable sendUpdatesToUI;
    Date currentTime;
    String timeString;

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


                //This is WORKS !!!
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

        LinearLayout archive = (LinearLayout) findViewById(R.id.menu_archive);
        archive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(Settings.ACTION_SETTINGS);
                intent1.putExtra("extra_prefs_show_button_bar", true);
                startActivity(intent1);
              //  startActivityForResult(intent1,0);
            }
        });



         sendUpdatesToUI = new Runnable() {
            public void run() {
 //               getSensorTemp();
                currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                timeString = sdf.format(currentTime );

                TextView temperatureTextView = (TextView) findViewById(R.id.main_activity_temperature);
                temperatureTextView.setText(String.valueOf(Math.round(sensorTemp))+ " °C");

                TextView timeTextView = (TextView) findViewById(R.id.main_activity_time);
                timeTextView.setText(timeString);

                handler.postDelayed(this, 1000); // 1 second
            }
        };
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

    }

    @Override
    public void onPause (){
        super.onPause();
        handler.removeCallbacks(sendUpdatesToUI);

    }
    @Override
    public void onStop() {
        super.onStop();
    }

/*

    private void getSensorTemp() {
        try {
            Max6675 max6675 = new Max6675();
            sensorTemp = max6675.getTemp();
            Log.i(LOG_TAG, "SensorTemp: " + sensorTemp + " °C");
            max6675.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}
