package com.example.android.mufflefurnace;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.mufflefurnace.ExecutionProgram.Max6675;

import java.io.IOException;
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
    String thermocoupleErrors = null;
    Max6675 max6675;

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
                Intent i = new Intent(MainActivity.this, ArchiveProgramsActivity.class);
                startActivity(i);
            }
        });



         sendUpdatesToUI = new Runnable() {
            public void run() {
                getSensorTemp();
                handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

                currentTime = Calendar.getInstance().getTime();

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                timeString = sdf.format(currentTime );

                TextView timeTextView = (TextView) findViewById(R.id.main_activity_time);
                timeTextView.setText(timeString);

                TextView temperatureTextView = (TextView) findViewById(R.id.main_activity_temperature);
                TextView errorsTextView = (TextView) findViewById(R.id.main_activity_errors);

                if (thermocoupleErrors == null){
                    errorsTextView.setVisibility(View.GONE);
                    temperatureTextView.setText(String.valueOf(Math.round(sensorTemp))+ " °C");
                } else {
                    temperatureTextView.setText("Error");
                    errorsTextView.setVisibility(View.VISIBLE);
                    errorsTextView.setText(thermocoupleErrors);
                }


            }
        };
 //       handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

        handler.post(sendUpdatesToUI);
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




    private void getSensorTemp() {
        try {
            max6675 = new Max6675();
            sensorTemp = max6675.getTemp();
            thermocoupleErrors = null;
            Log.i(LOG_TAG, "SensorTemp: " + sensorTemp + " °C");
            max6675.close();

        } catch (IOException e) {
            e.printStackTrace();
            try {
                max6675.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                thermocoupleErrors = e1.getMessage();
                Log.d(LOG_TAG, thermocoupleErrors);
                sensorTemp = 0;
            }
            sensorTemp = 0;
 //           thermocoupleErrors = "No thermocouple connected";
            thermocoupleErrors = e.getMessage();
            Log.d(LOG_TAG, thermocoupleErrors);
        }
    }



}
