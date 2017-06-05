package com.yapper.Yapper.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yapper.Yapper.R;
import com.yapper.Yapper.utils.ChatRoom;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference profile_root;
    private DatabaseReference subscribed_root;
    private String user_id;
    private TextView user_name_textview;
    private TextView email_textview;
    private ImageView profile_image_imageview;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user_name_textview = (TextView) findViewById(R.id.UserNameText);
        email_textview = (TextView) findViewById(R.id.EmailText);
        profile_image_imageview = (ImageView) findViewById(R.id.ProfileImage);
        layout = (LinearLayout) findViewById(R.id.ChatList);

        Bundle extras = getIntent().getExtras();
        user_id = getIntent().getExtras().getString("user_id");

        profile_root = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        subscribed_root = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("subscribed");

        profile_root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String user_name = (String) dataSnapshot.child("user_name").getValue();
                user_name_textview.setText(user_name);

                String email = (String) dataSnapshot.child("email").getValue();
                email_textview.setText(email);

                String picture = (String) dataSnapshot.child("photo_url").getValue();
                if (picture != null && !picture.isEmpty()) {
                    Picasso.with(ProfileActivity.this).load(picture).into(profile_image_imageview);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        subscribed_root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chatroom(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void append_chatroom(DataSnapshot dataSnapshot) {
        String chat;
        chat = (String) dataSnapshot.getValue();

        TextView text_view = new TextView(ProfileActivity.this);
        text_view.setText(chat);
        text_view.setGravity(Gravity.CENTER);

        layout.addView(text_view);
    }
}
