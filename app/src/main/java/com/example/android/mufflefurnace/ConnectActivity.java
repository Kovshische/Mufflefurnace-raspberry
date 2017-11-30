package com.example.android.mufflefurnace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ConnectActivity extends AppCompatActivity {

    private WifiElement[] nets;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private boolean wifiEnabled;

    private static String LOG_TAG = ProgramsActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);



        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiEnabled = wifiManager.isWifiEnabled();

        Log.i(LOG_TAG, "Check the log");

        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
            Log.i(LOG_TAG, "WIFI ON");
        }

        detectWifi();

        RelativeLayout wifiRefreshRelativeLayout = (RelativeLayout) findViewById(R.id.wifi_refresh);
        wifiRefreshRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // detectWifi();
                Log.i(LOG_TAG, "refresh wifi");



                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
    }

    class AdapterElements extends ArrayAdapter<Object> {
        Activity context;

        public AdapterElements(Activity context) {
            super(context, R.layout.list_wifi, nets);
            this.context =  context;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater layoutInflater = context.getLayoutInflater();
            View item = layoutInflater.inflate(R.layout.list_wifi, null);

            TextView tvSsid = (TextView) item.findViewById(R.id.tvSSID);
            tvSsid.setText(nets[position].getTitle());

            int tvLevel;
            String level=nets[position].getLevel();

            try {
                int i = Integer.parseInt(level);
                if (i > -50) {
                    tvLevel = 2; //High
                } else if (i <= -50 && i > -80) {
                    tvLevel = 1; //Middle
                } else if (i <= -80) {
                    tvLevel = 0; //Low
                }
            }catch (NumberFormatException e){
                Log.d(LOG_TAG, "incorrect string format");
            }
            return item;
        }
    }

    public void detectWifi(){
        this.wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.wifiManager.startScan();
        this.wifiList = this.wifiManager.getScanResults();

        Log.i (LOG_TAG, wifiList.toString());

        this.nets = new WifiElement[wifiList.size()];

        for(int i=0; i<wifiList.size(); i++){
            String item = wifiList.get(i).toString();
            String[] vector_item = item.split(",");
            String item_essid = vector_item[0];
            String item_capabilities = vector_item[2];
            String item_level = vector_item[3];
            String ssid = item_essid.split(":")[1];
            String security = item_capabilities.split(":")[1];
            String level = item_level.split(":")[1];
            nets[i] = new WifiElement(ssid, security, level);
        }

        AdapterElements adapterElements = new AdapterElements(this);
        ListView netList = (ListView) findViewById(R.id.list_view_wifi);
        netList.setAdapter(adapterElements);


    }
}
