package com.sarindev.cabapp.taker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sarindev.cabapp.R;

public class TakerLogin extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TakerLogin.class.getSimpleName();


    EditText et_taker_email;
    EditText et_taker_pwd;
    Button btn_taker_login;
    Button btn_taker_Registration;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    boolean isPermissionGranted=false;
    String[] mLocationPermissions = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int LOCATION_PERMISSION_CODE = 121;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taker);

        //Firebase AuthState changes "user" value to null if logged out.
        mAuth= FirebaseAuth.getInstance();
        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user=mAuth.getCurrentUser();
                Log.d(TAG,"onAuthStateChanged called");
                if (user!=null){
                    isPermissionGranted = checkLocationPermission();
                    if (isPermissionGranted) {
                        Intent giverMap = new Intent(TakerLogin.this, TakerMapActivity.class);
                        startActivity(giverMap);
                        finish();
                    }
                }
            }
        };

        et_taker_email=(EditText)findViewById(R.id.et_taker_email);
        et_taker_pwd=(EditText)findViewById(R.id.et_taker_password);
        btn_taker_login=(Button)findViewById(R.id.btn_taker_login);
        btn_taker_Registration=(Button)findViewById(R.id.btn_taker_Registration);
        btn_taker_login.setOnClickListener(this);
        btn_taker_Registration.setOnClickListener(this);

    }

    protected boolean checkLocationPermission(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(mLocationPermissions,LOCATION_PERMISSION_CODE);
            }
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSION_CODE: // if permission requested for ACCESS_FINE_LOCATION && ACCESS_COARSE_LOCATION
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1]== PackageManager.PERMISSION_GRANTED) ){
                    Toast.makeText(this,"Permisiions granted",Toast.LENGTH_SHORT).show();
                    Intent giverMap = new Intent(TakerLogin.this, TakerMapActivity.class);
                    startActivity(giverMap);
                    finish();
                }
                break;
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_taker_login:
                login();
                break;
            case R.id.btn_taker_Registration:
                register();
                break;
        }

    }

    private void login() {
        String email = et_taker_email.getText().toString();
        String pwd = et_taker_pwd.getText().toString();
        mAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(TakerLogin.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(TakerLogin.this,"Sign In error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void register() {
        String email = et_taker_email.getText().toString();
        String pwd = et_taker_pwd.getText().toString();
        mAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(TakerLogin.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(TakerLogin.this,"SignUp error",Toast.LENGTH_SHORT).show();
                }else {
                    String user_id = mAuth.getCurrentUser().getUid();
                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Taker").child(user_id);
                    current_user_db.setValue(true);

                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //adding auth listener for firebase
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //removing auth listener for firebase
        mAuth.removeAuthStateListener(authStateListener);
    }

}
