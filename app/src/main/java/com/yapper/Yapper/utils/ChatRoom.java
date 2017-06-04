package com.yapper.Yapper.utils;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yapper.Yapper.R;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jteo1 on 6/2/2017.
 */

public class ChatRoom extends AppCompatActivity {
    private LinearLayout layout;
    private Button btn_send_msg;
    private EditText input_msg;
    private ScrollView scroll_view;
    private DatabaseReference chatrooms_root;
    private String temp_key;

    private String user_id, username, room_id;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);

        layout = (LinearLayout) findViewById(R.id.layout1);
        btn_send_msg = (Button) findViewById(R.id.btn_send);
        input_msg = (EditText) findViewById(R.id.msg_input);
        scroll_view = (ScrollView) findViewById(R.id.scrollView);

        // TODO: fill in the appropriate key for room_name later: getIntent().getExtras().get("")
        room_id = "halp";

        // TODO: get these from auth
        user_id = "123456";
        username = "jteo1";

        chatrooms_root = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(room_id).child("messages");

        // callback to fetch and display room name from firebase
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(room_id).child("room_name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setTitle((String) dataSnapshot.getValue());
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

                DatabaseReference message_root = chatrooms_root.child(temp_key);
                Map<String, Object> name_and_message = new HashMap<String, Object>();
                name_and_message.put("user_name", username);
                name_and_message.put("timestamp", DateFormat.getDateTimeInstance().format(new Date()));
                name_and_message.put("body", input_msg.getText().toString());
                name_and_message.put("user_id", user_id);

                message_root.updateChildren(name_and_message);

                input_msg.setText("");

            }
        });

        //iterates over each message in a chatroom
        chatrooms_root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chat(dataSnapshot);

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



    //append a single message to the message stream
    private void append_chat(DataSnapshot dataSnapshot) {
        String chat_msg, chat_user_id, timestamp, chat_user_name;

        chat_msg = (String) dataSnapshot.child("body").getValue();
        timestamp = (String) dataSnapshot.child("timestamp").getValue();
        chat_user_id = (String) dataSnapshot.child("user_id").getValue();
        chat_user_name = (String) dataSnapshot.child("user_name").getValue();

        TextView text_view = new TextView(ChatRoom.this);

        //make user name clickable to go to their profile page
        SpannableString ss = new SpannableString(chat_user_name + " (" + timestamp + "):\n" + chat_msg);
        ss.setSpan(new MyClickableSpan(chat_user_id), 0, chat_user_name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //TODO: update UI for message stream
        text_view.setText(ss);
        text_view.setMovementMethod(LinkMovementMethod.getInstance());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        text_view.setLayoutParams(lp);
        text_view.setBackgroundColor(Color.parseColor("#dddddd"));

        layout.addView(text_view);

    }



    class MyClickableSpan extends ClickableSpan {
        String user_id;

        MyClickableSpan(String user_id) {
            this.user_id = user_id;
        }

        public void onClick(View text_view) {
            //TODO: go to new activity instead of toast
            Toast.makeText(ChatRoom.this, user_id, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(Color.BLACK);//set text color
            ds.setUnderlineText(false); // set to false to remove underline
        }
    }

}

