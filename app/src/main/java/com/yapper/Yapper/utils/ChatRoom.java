package com.yapper.Yapper.utils;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yapper.Yapper.R;
import com.yapper.Yapper.ui.ProfileActivity;
import com.yapper.Yapper.ui.chatrooms.ChatroomListActivity;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jteo1 on 6/2/2017.
 */

public class ChatRoom extends AppCompatActivity {

    public static final String ROOM_ID_KEY = "room_id";

    private LinearLayout layout;
    private Button btn_send_msg;
    private EditText input_msg;
    private ScrollView scroll_view;
    private DatabaseReference chatrooms_root;
    private String temp_key;

    private String user_id, username, room_id, room_name;

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            finish();
            startActivity(new Intent(this, ChatroomListActivity.class));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DatabaseReference room_subscribers = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(room_id).child("subscribers");
        DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("subscribed");


        switch (item.getItemId()) {
            case R.id.subscribe:
                // add user as subscribed to chatroom

                Map<String, Object> id_and_username = new HashMap<String, Object>();
                id_and_username.put(user_id, username);
                room_subscribers.updateChildren(id_and_username);

                //add chatroom to list of chatrooms they are subscribed to
                Map<String, Object> id_and_room = new HashMap<String, Object>();
                id_and_room.put(room_id, room_name);
                users.updateChildren(id_and_room);

                Toast.makeText(this, "Subscribed!", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.unsubscribe:
                //reverse operation of above case
                room_subscribers.child(user_id).removeValue();
                users.child(room_id).removeValue();

                Toast.makeText(this, "Unsubscribed!", Toast.LENGTH_SHORT).show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);

        layout = (LinearLayout) findViewById(R.id.layout1);
        btn_send_msg = (Button) findViewById(R.id.btn_send);
        input_msg = (EditText) findViewById(R.id.msg_input);
        scroll_view = (ScrollView) findViewById(R.id.scrollView);

        Intent args = getIntent();
        room_id = args.getStringExtra(ROOM_ID_KEY);
        if (room_id == null) {
            room_id = "halp";
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user_id = user.getUid();

        chatrooms_root = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(room_id).child("messages");

        //get username based on user_id retrieved from firebase auth
        FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("user_name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = (String) dataSnapshot.getValue();

                // callback to fetch and display room name from firebase
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(room_id).child("room_name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        room_name = (String) dataSnapshot.getValue();
                        setTitle(room_name);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                btn_send_msg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Map<String, Object> messages = new HashMap<String, Object>();
                        temp_key = chatrooms_root.push().getKey();
                        chatrooms_root.updateChildren(messages);

                        //avoid blank inputs
                        String msg = input_msg.getText().toString().trim();
                        if(!msg.equals("")) {
                            DatabaseReference message_root = chatrooms_root.child(temp_key);
                            Map<String, Object> name_and_message = new HashMap<String, Object>();
                            name_and_message.put("user_name", username);
                            name_and_message.put("timestamp", System.currentTimeMillis());
                            name_and_message.put("body", msg);
                            name_and_message.put("user_id", user_id);

                            message_root.updateChildren(name_and_message);

                            input_msg.setText("");
                        }
                    }
                });

                //iterates over each message in a chatroom
                chatrooms_root.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        append_chat(dataSnapshot, username);

                        //scroll to bottom
                        scroll_view.post(new Runnable() {
                            @Override
                            public void run() {
                                scroll_view.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    //append a single message to the message stream
    private void append_chat(DataSnapshot dataSnapshot, String username) {
        String chat_msg, chat_user_id, timestamp, chat_user_name;

        chat_msg = (String) dataSnapshot.child("body").getValue();
        Date date = new Date((Long) dataSnapshot.child("timestamp").getValue());
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd, hh:mm a");
        timestamp = formatter.format(date);
        chat_user_id = (String) dataSnapshot.child("user_id").getValue();
        chat_user_name = (String) dataSnapshot.child("user_name").getValue();

        TextView text_view = new TextView(ChatRoom.this);

        //make user name clickable to go to their profile page
        SpannableString ss = new SpannableString(chat_user_name + " (" + timestamp + ")\n" + chat_msg);
        ss.setSpan(new MyClickableSpan(chat_user_id), 1, chat_user_name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        text_view.setText(ss);
        text_view.setMovementMethod(LinkMovementMethod.getInstance());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //if messages are from the current user, use a lighter color and right alignment
        if(username.equals(chat_user_name)) {
            text_view.setBackgroundColor(Color.parseColor("#f0f0f0"));
            text_view.setGravity(Gravity.RIGHT);
        } else {
            text_view.setBackgroundColor(Color.parseColor("#dddddd"));
        }
        lp.setMargins(0, 0, 0, 20);
        text_view.setLayoutParams(lp);


        layout.addView(text_view);

    }

    void start_profile_intent(String user_id) {
        Intent intent = new Intent(this , ProfileActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }

    class MyClickableSpan extends ClickableSpan {
        String user_id;

        MyClickableSpan(String user_id) {
            this.user_id = user_id;
        }

        public void onClick(View text_view) {
            start_profile_intent(user_id);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false); // set to false to remove underline
        }
    }

}

