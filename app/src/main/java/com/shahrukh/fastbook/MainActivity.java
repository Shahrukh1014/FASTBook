package com.shahrukh.fastbook;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView userposts;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBar;
    private FirebaseAuth maAuth;
    private DatabaseReference userReference;
    private CircleImageView profilePic;
    private TextView userNameTextView;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar= findViewById(R.id.appbarlayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = findViewById(R.id.drawerLayout);
        actionBar = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);


        navigationView= findViewById(R.id.navigationView);
        drawerLayout.addDrawerListener(actionBar);

        actionBar.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        profilePic = navView.findViewById(R.id.circleImageView);
        userNameTextView = navView.findViewById(R.id.Username);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;


            }
        });

        maAuth= FirebaseAuth.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            sendUsertoLoginActivity();
        }
        else {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userReference = FirebaseDatabase.getInstance().getReference().child("Users");


            userReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("Full_Name")) {
                            String fullname = dataSnapshot.child("Full_Name").getValue().toString();
                            userNameTextView.setText(fullname);
                        }
                        if (dataSnapshot.hasChild("profileimage")) {
                            String image = dataSnapshot.child("profileimage").getValue().toString();
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(profilePic);
                        } else {
                            Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser currentUser=maAuth.getCurrentUser();
        if(currentUser==null){
            sendUsertoLoginActivity();
        }else{
            checkUserExistence();
        }
    }

    private void checkUserExistence() {
        final String currentUserId= maAuth.getCurrentUser().getUid();
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(currentUserId)){
                    sendUsertoSetupPage();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
  }
    private void sendUsertoSetupPage() {
        Intent porfileSetup= new Intent(MainActivity.this, ProfileSetupActivity.class);
        porfileSetup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(porfileSetup);
        finish();
    }

    public  void sendUsertoLoginActivity(){
        Intent loginIntent= new Intent(MainActivity.this, login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBar.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void UserMenuSelector(MenuItem item){
            switch (item.getItemId()){
                case R.id.new_post:
                    Toast.makeText(getApplicationContext(), "New Post Selected", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.my_profile:
                    Toast.makeText(getApplicationContext(), "Your Profile Selected", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.home:
                    Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.friends:
                    Toast.makeText(getApplicationContext(), "Friends Selected", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.find_friends:
                    Toast.makeText(getApplicationContext(), "Find Friends Selected", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.messages:
                    Toast.makeText(getApplicationContext(), "Messages Selected", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.settings:
                    Toast.makeText(getApplicationContext(), "Settings Selected", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.logout:
                    maAuth.signOut();
                    sendUsertoLoginActivity();
                    break;
            }
    }
}
