package com.yapper.Yapper.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import com.yapper.Yapper.R;
import com.yapper.Yapper.utils.ChatRoom;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    public void sendMessage(View view){
        Intent intent = new Intent(this, ChatRoom.class);
        startActivity(intent);
    }
}
