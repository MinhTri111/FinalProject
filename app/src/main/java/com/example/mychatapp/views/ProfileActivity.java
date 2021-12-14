package com.example.mychatapp.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mychatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserId, Current_state, sender_userID;
    private CircleImageView userprofileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessagesRequestButton,DeclineMessageRequestButton;
    private DatabaseReference UserRef, ChatRequestRef, ContactRef,NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");
         receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        Toast.makeText(this, "UserId: "+ receiverUserId , Toast.LENGTH_SHORT).show();
        userprofileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_profile_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMessagesRequestButton =(Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton =(Button) findViewById(R.id.decline_message_request_button);

        Current_state = "new";
        mAuth = FirebaseAuth.getInstance();
        RetrieveUserInfo();
        sender_userID = mAuth.getCurrentUser().getUid();


    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && (snapshot.hasChild("image"))){
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userprofileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequestS();
                }
                else {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequestS();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void ManageChatRequestS() {
            ChatRequestRef.child(sender_userID)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserId)){
                                String request_type= snapshot.child(receiverUserId).child("request_type").getValue().toString();
                                if(request_type.equals("sent")){
                                    Current_state="request_sent";
                                    SendMessagesRequestButton.setText("Cancel Chat Request");
                                }
                                else if(request_type.equals("received")){
                                    Current_state="request_received";
                                    SendMessagesRequestButton.setText("Accept Chat Request");
                                    DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                    DeclineMessageRequestButton.setEnabled(true);
                                    DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            CancelChatRequest();
                                        }
                                    });
                                }
                            }
                            else{
                                ContactRef.child(sender_userID)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.hasChild(receiverUserId)){
                                                    Current_state = "friends";
                                                    SendMessagesRequestButton.setText("Remove this contact");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        if(!sender_userID.equals(receiverUserId)){
            SendMessagesRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendMessagesRequestButton.setEnabled(false);
                    if(Current_state.equals("new")){
                        SendChatRequest();
                    }
                    if(Current_state.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if(Current_state.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if(Current_state.equals("friends")){
                        RemoveSpecificContact();
                    }

                }
            });
        }
        else{
            SendMessagesRequestButton.setVisibility(View.INVISIBLE);
        }




    }

    private void RemoveSpecificContact() {
        ContactRef.child(sender_userID).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ContactRef.child(receiverUserId).child(sender_userID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendMessagesRequestButton.setEnabled(true);
                                                Current_state="new";
                                                SendMessagesRequestButton.setText("Send Message") ;

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactRef.child(sender_userID).child(receiverUserId)
                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    ContactRef.child(receiverUserId).child(sender_userID)
                            .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                ChatRequestRef.child(sender_userID).child(receiverUserId).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                ChatRequestRef.child(receiverUserId).child(sender_userID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                        SendMessagesRequestButton.setEnabled(true);
                                                                        Current_state = "friends";
                                                                        SendMessagesRequestButton.setText("Remove this contact");
                                                                        DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                        DeclineMessageRequestButton.setEnabled(false);

                                                            }
                                                        });
                                            }
                                        });

                            }
                        }
                    });
                }
            }
        });

    }

    private void CancelChatRequest() {
        ChatRequestRef.child(sender_userID).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                      if(task.isSuccessful()){
                          ChatRequestRef.child(receiverUserId).child(sender_userID)
                                  .removeValue()
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          if(task.isSuccessful()){
                                                    SendMessagesRequestButton.setEnabled(true);
                                                    Current_state="new";
                                                    SendMessagesRequestButton.setText("Send Message") ;

                                                    DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                    DeclineMessageRequestButton.setEnabled(false);
                                          }
                                      }
                                  });
                      }
                    }
                });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(sender_userID).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ChatRequestRef.child(receiverUserId).child(sender_userID)
                                    .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        HashMap<String,String> chatnotificationMap = new HashMap<>();
                                        chatnotificationMap.put("from",sender_userID);
                                        chatnotificationMap.put("type","request");
                                        NotificationRef.child(receiverUserId).push()
                                                .setValue(chatnotificationMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            SendMessagesRequestButton.setEnabled(true);
                                                            Current_state = "request_sent";
                                                            SendMessagesRequestButton.setText("Cancel Chat Request");
                                                        }
                                                    }
                                                });



                                    }
                                }
                            });
                        }
                    }
                });
    }
}