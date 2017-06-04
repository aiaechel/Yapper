package com.yapper.Yapper.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yapper.Yapper.R;
import com.yapper.Yapper.utils.ChatRoom;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference profile_root;
    private TextView user_name_textview;
    private TextView email_textview;
    private TextView description_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user_name_textview = (TextView) findViewById(R.id.UserNameText);
        email_textview = (TextView) findViewById(R.id.EmailText);
        description_textview = (TextView) findViewById(R.id.DescriptionText);

        profile_root = FirebaseDatabase.getInstance().getReference().child("users").child("userID");

        profile_root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String user_name = (String) dataSnapshot.child("user_name").getValue();
                user_name_textview.setText(user_name);

                String email = (String) dataSnapshot.child("email").getValue();
                email_textview.setText(email);

                //String description = (String) dataSnapshot.child("").getValue();
                //description_textview.setText(description);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    public void sendMessage(View view){
        Intent intent = new Intent(this, ChatRoom.class);
        startActivity(intent);
    }
}