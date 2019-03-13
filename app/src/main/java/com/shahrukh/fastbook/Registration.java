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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Registration extends AppCompatActivity {

    private EditText email, password, confirmPassword;
    private Button createAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        email= findViewById(R.id.email);
        password= findViewById(R.id.password);
        confirmPassword= findViewById(R.id.confirmpassword);
        createAccount= findViewById(R.id.create_account);
        mAuth= FirebaseAuth.getInstance();
        loading= new ProgressDialog(this);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            sendUsertoHome();
        }
    }
    protected void sendUsertoHome(){
        Intent toHome= new Intent(Registration.this, MainActivity.class);
        toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toHome);
        finish();
    }



    private void createNewAccount(){
        String userEmail=email.getText().toString();
        String userPassword=password.getText().toString();
        String userConfirmPassword=confirmPassword.getText().toString();

        if(TextUtils.isEmpty(userEmail)){
            Toast.makeText(this, "Please Write your Email", Toast.LENGTH_SHORT).show();
        }else  if (!userEmail.endsWith("@nu.edu.pk")){
            Toast.makeText(this, "Please Writex NU Mail", Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(userPassword)){
            Toast.makeText(this, "Please Write your Password", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(userConfirmPassword)){
            Toast.makeText(this, "Please Confirm your Passwoord", Toast.LENGTH_SHORT).show();
        }
        else if(!userPassword.equals(userConfirmPassword)){
            Toast.makeText(this, "Password doesnt Match :( ", Toast.LENGTH_SHORT).show();
        }
        else {
            loading.setTitle("Creating your new Account");
            loading.setMessage("Wait kro Bhai..");
            loading.show();
            loading.setCanceledOnTouchOutside(true);
            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendUserToSetupProfile();
                                loading.dismiss();
                                Toast.makeText(Registration.this, "You are authenticated Successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                String message=task.getException().getMessage();
                                Toast.makeText(Registration.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        }
                    });


        }

    }
    private void sendUserToSetupProfile(){
        Intent setUpProfile= new Intent(Registration.this, ProfileSetupActivity.class);
        setUpProfile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setUpProfile);
        finish();

    }
}
