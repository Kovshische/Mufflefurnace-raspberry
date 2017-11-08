package com.example.android.mufflefurnace;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout settings = (RelativeLayout) findViewById(R.id.menu_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });

        RelativeLayout programs = (RelativeLayout) findViewById(R.id.menu_programs);
        programs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ProgramsActivity.class);
                startActivity(i);
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        float density = getResources().getDisplayMetrics().density;

        TextView TextViewWidth = (TextView)findViewById(R.id.displayWidth);
        TextViewWidth.setText("Width " + Integer.toString(width));

        TextView TextViewHight = (TextView)findViewById(R.id.displayHeight);
        TextViewHight.setText("Height " + Integer.toString(height));

        TextView TextViewDencity = (TextView)findViewById(R.id.displayDencity);
        TextViewDencity.setText("Density " + Float.toString(density));
    }
}
