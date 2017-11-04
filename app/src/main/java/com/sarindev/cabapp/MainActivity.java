package com.sarindev.cabapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sarindev.cabapp.giver.GiverLogin;
import com.sarindev.cabapp.taker.TakerLogin;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_giver;
    Button btn_taker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_giver=(Button)findViewById(R.id.btn_giver);
        btn_taker=(Button)findViewById(R.id.btn_taker);
        btn_giver.setOnClickListener(this);
        btn_taker.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
       switch (view.getId()){
           case R.id.btn_giver:
               Intent giverIntent=new Intent(MainActivity.this,GiverLogin.class);
               startActivity(giverIntent);
               finish();
               break;
           case R.id.btn_taker:
               Intent takerIntent=new Intent(MainActivity.this,TakerLogin.class);
               startActivity(takerIntent);
               finish();
               break;
       }
    }
}
