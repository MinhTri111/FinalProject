package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatsActivity extends AppCompatActivity {
    private androidx.appcompat.widget.Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;
    private String currentGroupName, currentUserID, currentUserName, saveCurrentDate,saveCurrentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,GroupNameRef,GroupMessageKeyRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chats);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        GroupMessageKeyRef= FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        InitializeField();
        GetUserInfo();
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoToDataBase();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
                if(snapshot.exists()){
                    DisplayMessage(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    DisplayMessage(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void DisplayMessage(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext()){
            String chatDate  = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage  = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName  = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime  = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append("From: "+chatName+ "\n" +chatMessage + "\n"+chatTime+"   "+chatDate+ "\n\n\n" );


            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }


    }


    private void SaveMessageInfoToDataBase() {
        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(GroupChatsActivity.this, "Please write your message", Toast.LENGTH_SHORT).show();

        }
        else{
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currendate =  new SimpleDateFormat("MMM dd, yyyy");
            saveCurrentDate = currendate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currenTime =  new SimpleDateFormat("hh:mm a");
            saveCurrentTime = currenTime.format(calFordTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef  = GroupNameRef.child(messageKey);

            HashMap<String,Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name",currentUserName);
                messageInfoMap.put("message",message);
                messageInfoMap.put("date",saveCurrentDate);
                messageInfoMap.put("time",saveCurrentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }

    }

    private void GetUserInfo() {
            UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        currentUserName = snapshot.child("name").getValue().toString();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    private void InitializeField() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button_group);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessage = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView  = (ScrollView) findViewById(R.id.my_scroll_view);




    }
}