package com.shahrukh.fastbook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shahrukh.fastbook.R;

public class login extends AppCompatActivity {
    private Button loginB;
    private EditText userEmail, userPassword;
    private TextView newAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        newAccountLink = findViewById(R.id.newAccount);
        userEmail = findViewById(R.id.email_login);
        userPassword = findViewById(R.id.password_login);
        loginB = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
        loading = new ProgressDialog(this);
        newAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUsertoRegsiterActivity();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sendUserToHome();
        }
    }

    protected void userLogin(View v){
//        Toast.makeText(login.this, "Error: ", Toast.LENGTH_SHORT).show();
        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Email tou likh lo Bhai.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password nahi likhna?", Toast.LENGTH_SHORT).show();
        }
        else{

            loading.setTitle("LogIN Ho RAHA HAI :o");
            loading.setMessage("Wait kro Bhai..");
            loading.show();
            loading.setCanceledOnTouchOutside(true);
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(login.this, "Login hogye bhai", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                                sendUserToHome();
                            }else{
                                String msg= task.getException().getMessage();
                                Toast.makeText(login.this, "Error: "+msg, Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        }
                    });
        }
    }
    private void sendUserToHome() {
        Intent toHome = new Intent(login.this, MainActivity.class);
        toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toHome);
        finish();
    }

    private void sendUsertoRegsiterActivity() {
        Intent userRegistration = new Intent(login.this, Registration.class);
        startActivity(userRegistration);

    }
}
