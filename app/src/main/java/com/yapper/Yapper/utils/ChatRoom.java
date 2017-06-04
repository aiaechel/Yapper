package com.yapper.Yapper.utils;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yapper.Yapper.R;
import com.yapper.Yapper.models.chatrooms.LatLng;

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
    private DatabaseReference root;
    private String temp_key;

    private String user_id, room_id;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);

        layout = (LinearLayout) findViewById(R.id.layout1);
        btn_send_msg = (Button) findViewById(R.id.btn_send);
        input_msg = (EditText) findViewById(R.id.msg_input);
        scroll_view = (ScrollView) findViewById(R.id.scrollView);

        // TODO: fill in the appropriate key for user_name and room_name later: getIntent().getExtras().get("")
        user_id = "userID";
        room_id = "halp";

        root = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(room_id).child("messages");

        setTitle(room_id);

        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> messages = new HashMap<String, Object>();
                temp_key = root.push().getKey();
                root.updateChildren(messages);

                DatabaseReference message_root = root.child(temp_key);
                Map<String, Object> name_and_message = new HashMap<String, Object>();
                name_and_message.put("username", user_id);
                //TODO: generate timestamp
                name_and_message.put("timestamp", DateFormat.getDateTimeInstance().format(new Date()));
                name_and_message.put("body", input_msg.getText().toString());

                message_root.updateChildren(name_and_message);

                input_msg.setText("");

            }
        });

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chat(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_chat(dataSnapshot);
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

    private String chat_msg, chat_user_name, timestamp;

    private void append_chat(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();
        while(i.hasNext()) {
            //get body, timestamp, and user in that order
            chat_msg = (String) ((DataSnapshot)i.next()).getValue();
            timestamp = (String) ((DataSnapshot)i.next()).getValue();
            chat_user_name = (String) ((DataSnapshot)i.next()).getValue();

            TextView text_view = new TextView(ChatRoom.this);

            //make user name clickable to go to their profile page
            SpannableString ss = new SpannableString(chat_user_name + " (" + timestamp + "):\n" + chat_msg);
            ss.setSpan(new MyClickableSpan(), 0, chat_user_name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            //TODO: update UI for message stream
            text_view.setText(ss);
            text_view.setMovementMethod(LinkMovementMethod.getInstance());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 10);
            text_view.setLayoutParams(lp);
            text_view.setBackgroundColor(Color.parseColor("#dddddd"));

            layout.addView(text_view);
            scroll_view.fullScroll(View.FOCUS_DOWN);
        }
    }



    class MyClickableSpan extends ClickableSpan {
        public void onClick(View text_view) {
            //go to new activity
            Toast.makeText(ChatRoom.this, "Clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(Color.BLACK);//set text color
            ds.setUnderlineText(false); // set to false to remove underline
        }
    }

}

