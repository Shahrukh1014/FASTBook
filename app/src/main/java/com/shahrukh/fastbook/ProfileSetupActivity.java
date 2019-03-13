package com.shahrukh.fastbook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText userName, fullName, country;
    private Button saveInfo;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference userReference;
    String currentUserId;
    private ProgressDialog loading;
    final static int galleryPic=1;
    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        mAuth=FirebaseAuth.getInstance();
        loading=new ProgressDialog(this);
        currentUserId=mAuth.getCurrentUser().getUid();

        userReference= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Image");
        userName=findViewById(R.id.userName);
        fullName=findViewById(R.id.fullName);
        country=findViewById(R.id.country);
        saveInfo=findViewById(R.id.saveInfoButton);
        profileImage= findViewById(R.id.profileImage);
        loadingBar = new ProgressDialog(this);


        saveInfo.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                saveAccountInfo();
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery= new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, galleryPic);
            }
        });
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();

                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                    }
                    else
                    {
                        Toast.makeText(ProfileSetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryPic && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileSetupActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            userReference.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Intent selfIntent = new Intent(ProfileSetupActivity.this, ProfileSetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(ProfileSetupActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            } else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(ProfileSetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void saveAccountInfo(){
        String name=userName.getText().toString();
        String userFullName=fullName.getText().toString();
        String usserCountry=country.getText().toString();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Email tou likh lo Bhai.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(userFullName)){
            Toast.makeText(this, "Full name kiya hai??", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(usserCountry)){
            Toast.makeText(this, "country likho Jani?", Toast.LENGTH_SHORT).show();
        }
        else{
            loading.setTitle("Saving Information");
            loading.setMessage("Please wait, while we are creating your new Account...");
            loading.show();
            loading.setCanceledOnTouchOutside(true);

            HashMap usermMap = new HashMap();
            usermMap.put("Username", name);
            usermMap.put("Full_Name", userFullName);
            usermMap.put("User_Country", usserCountry);
            usermMap.put("Department", "xyz-default");
            usermMap.put("Gender", "Not Selected");
            usermMap.put("DOB", "");
            usermMap.put("R_Status", "NULL");
            //usermMap.put("profilepic", "")
            userReference.updateChildren(usermMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        senduserToHome();
                        Toast.makeText(ProfileSetupActivity.this, "your Account is created Successfully.", Toast.LENGTH_LONG).show();
                        loading.dismiss();
                    }
                    else
                    {
                        String message =  task.getException().getMessage();
                        Toast.makeText(ProfileSetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }

            });


        }
    }
    private void senduserToHome() {
            Intent mainIntent = new Intent(ProfileSetupActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();

    }


}
