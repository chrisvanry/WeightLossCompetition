package com.chrisvanry.weightlosscontest.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chrisvanry.weightlosscontest.R;
import com.chrisvanry.weightlosscontest.data.Competition;
import com.chrisvanry.weightlosscontest.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    // TODO hamburger menu
    // TODO back button should return to HOME
    // TODO proper hide/view of buttons
    // TODO settings button

    private static final String TAG = "ViewDatabase";

    private TextView textViewCurrentComp;

    // Firebase stuff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Nav buttons
        // Button buttonSettings = findViewById(R.id.buttonSettings);
        Button buttonLogout = findViewById(R.id.buttonLogout);

        textViewCurrentComp = findViewById(R.id.textViewCurrentComp);
        Button buttonCompetition = findViewById(R.id.buttonCompetition);
        Button buttonJoinComp = findViewById(R.id.buttonJoinComp);
        Button buttonCreateComp = findViewById(R.id.buttonCreateComp);
        Button buttonRecordWeight = findViewById(R.id.buttonRecordWeight);

        // Firebase stuff
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            // Reads database once on creation and each time a change is made
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = getUserData(dataSnapshot);
                String compID = currentUser.getCompetitionId();
                String notEnrolled = "not enrolled";
                // If user not enrolled, show text and hide details and record buttons
                if (compID.equals(notEnrolled)){
                    textViewCurrentComp.setText("- Not Enrolled -");
                    buttonJoinComp.setVisibility(View.VISIBLE);
                    buttonCreateComp.setVisibility(View.VISIBLE);
                    // If user enrolled, display comp name and hide create/join button
                } else {
                    Competition currentComp = getCompData(dataSnapshot, compID);
                    textViewCurrentComp.setText(currentComp.getName());
                    buttonCompetition.setVisibility(View.VISIBLE);
                    buttonRecordWeight.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // OnClick listener for logout button
        buttonLogout.setOnClickListener(v -> {
            // Logout and redirect to login screen
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            toastMessage("Logout successful");
            finish();
        });

        // OnClick listener for create competition button
        buttonCreateComp.setOnClickListener(v -> {
            // direct to create comp screen
            Intent intent = new Intent(getApplicationContext(), CreateComp.class);
            startActivity(intent);
            finish();
        });

    }

    private User getUserData(DataSnapshot dataSnapshot) {

        User currentUser = new User();

        currentUser.setFirstName(dataSnapshot.child("Users").child(userID).child("firstName").getValue().toString());
        currentUser.setLastName(dataSnapshot.child("Users").child(userID).child("lastName").getValue().toString());
        currentUser.setEmail(dataSnapshot.child("Users").child(userID).child("email").getValue().toString());
        currentUser.setCompetitionId(dataSnapshot.child("Users").child(userID).child("competitionId").getValue().toString());

        // display all the information
        Log.d(TAG, "showData: First name: " + currentUser.getFirstName());
        Log.d(TAG, "showData: Last name: " + currentUser.getLastName());
        Log.d(TAG, "showData: Email: " + currentUser.getEmail());
        Log.d(TAG, "showData: Comp ID: " + currentUser.getCompetitionId());

        return currentUser;
    }

    private Competition getCompData(DataSnapshot dataSnapshot, String compID) {

        Competition currentComp = new Competition();

        currentComp.setName(dataSnapshot.child("Competitions").child(compID).child("name").getValue().toString());
        currentComp.setStartDate(dataSnapshot.child("Competitions").child(compID).child("startDate").getValue().toString());
        currentComp.setLength(dataSnapshot.child("Competitions").child(compID).child("length").getValue().toString());

        // display all the information
        Log.d(TAG, "showData: Comp name: " + currentComp.getName());
        Log.d(TAG, "showData: Start date: " + currentComp.getStartDate());
        Log.d(TAG, "showData: Length: " + currentComp.getLength());

        return currentComp;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

}