package com.sarindev.cabapp.giver;

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
import com.sarindev.cabapp.R;

public class GiverLogin extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = GiverLogin.class.getSimpleName();

    EditText et_giver_email;
    EditText et_giver_pwd;
    Button btn_giver_login;
    Button btn_giver_Registration;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giver);

        //Firebase AuthState changes "user" value to null if logged out.
        mAuth= FirebaseAuth.getInstance();
        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user=mAuth.getCurrentUser();
                Log.d(TAG,"onAuthStateChanged called");
                if (user!=null){
                    Log.d(TAG,"user = "+user.getEmail());
                    Intent giverMap= new Intent(GiverLogin.this,GiverMapActivity.class);
                    startActivity(giverMap);
                    finish();
                }
            }
        };

        et_giver_email=(EditText)findViewById(R.id.et_giver_email);
        et_giver_pwd=(EditText)findViewById(R.id.et_giver_password);
        btn_giver_login=(Button)findViewById(R.id.btn_giver_login);
        btn_giver_Registration=(Button)findViewById(R.id.btn_giver_Registration);
        btn_giver_login.setOnClickListener(this);
        btn_giver_Registration.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_giver_login:
                login();
                break;
            case R.id.btn_giver_Registration:
                register();
                break;
        }

    }

    private void login() {
        String email = et_giver_email.getText().toString();
        String pwd = et_giver_pwd.getText().toString();
        mAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(GiverLogin.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(GiverLogin.this,"Sign In error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void register() {
        String email = et_giver_email.getText().toString();
        String pwd = et_giver_pwd.getText().toString();
        mAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(GiverLogin.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(GiverLogin.this,"SignUp error",Toast.LENGTH_SHORT).show();
                }else {
                    String user_id = mAuth.getCurrentUser().getUid();
                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Giver").child(user_id);
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