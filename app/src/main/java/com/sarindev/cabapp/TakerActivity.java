package com.sarindev.cabapp;

import android.content.Intent;
import android.support.annotation.NonNull;
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

public class TakerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TakerActivity.class.getSimpleName();

    EditText et_taker_email;
    EditText et_taker_pwd;
    Button btn_taker_login;
    Button btn_taker_Registration;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

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
                    Intent giverMap= new Intent(TakerActivity.this,MapActivity.class);
                    startActivity(giverMap);
                    finish();
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
        mAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(TakerActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(TakerActivity.this,"Sign In error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void register() {
        String email = et_taker_email.getText().toString();
        String pwd = et_taker_pwd.getText().toString();
        mAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(TakerActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(TakerActivity.this,"SignUp error",Toast.LENGTH_SHORT).show();
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
