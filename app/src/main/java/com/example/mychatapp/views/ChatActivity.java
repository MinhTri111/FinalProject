package com.example.mychatapp.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mychatapp.MessagesAdapter;
import com.example.mychatapp.R;
import com.example.mychatapp.model.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID,messageReceiverName,messageReceiverImage, messageSenderID,saveCurrenTime,saveCurrenDate;
    private TextView userName, userLastseen;
    private CircleImageView userImage;
    private Toolbar ChatToolBar;
    private ImageButton SendMessageButton,SendFilesButton;
    private EditText MessageInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView userMessagesList;
    private String checker="",myurl="";
    private ProgressDialog loadingBar;
    private StorageTask uploadTask;
    private Uri fileuri;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();
        RootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();


        IntializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
        DisplayLastSeen();
        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"
                        };
                AlertDialog.Builder builder= new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);

                        }
                        if(i==1){
                            checker = "pdf";
                        }
                        if(i==2){
                            checker = "docx";
                        }
                    }
                });
                builder.show();
                }
        });
        RootRef.child("Message").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
                        Messages messages = snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messagesAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 &&  resultCode== RESULT_OK && data!=null && data.getData()!=null){
            loadingBar.setTitle("Sending Image");
            loadingBar.setMessage("Please wait, we are sending...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            fileuri = data.getData();
            if(!checker.equals("image")){

            }
            else if(checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String message_sender_ref = "Message/" + messageSenderID + "/" + messageReceiverID;
                final String message_receiver_ref = "Message/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference user_message_key = RootRef.child("Message").child(messageSenderID).child(messageReceiverID).push();
                final String message_push_id = user_message_key.getKey();
                final StorageReference filePath = storageReference.child(message_push_id+ "."+"jpg");
                uploadTask = filePath.putFile(fileuri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                     public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri dowloadUrl = task.getResult();
                            myurl = dowloadUrl.toString();
                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",myurl);
                            messageTextBody.put("name",fileuri.getLastPathSegment());

                            messageTextBody.put("time",saveCurrenTime);
                            messageTextBody.put("date",saveCurrenDate);
                            messageTextBody.put("type",checker);
                            messageTextBody.put("messageID ",message_push_id);
                            messageTextBody.put("from",messageSenderID);
                            messageTextBody.put("to",messageReceiverID);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id,messageTextBody);
                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id,messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        loadingBar.dismiss();

                                    }
                                    else{
                                        loadingBar.dismiss();
                                        String message = task.getException().getMessage();
                                        Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();

                                    }
                                    MessageInputText.setText("");

                                }
                            });
                        }
                    }
                });
            }
            else{
                loadingBar.dismiss();
                Toast.makeText(this,"Nothing selected. Error",Toast.LENGTH_SHORT).show();

            }
        }

    }

    private void IntializeControllers() {



        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater =(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);

        actionBar.setCustomView(actionbarView);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastseen = (TextView) findViewById(R.id.custom_user_last_seen);
        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        messagesAdapter = new MessagesAdapter(messagesList);
        userMessagesList =   (RecyclerView)findViewById(R.id.private_messages_list_of_user);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currendate =  new SimpleDateFormat("MMM dd, yyyy");
        saveCurrenDate = currendate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currenTime =  new SimpleDateFormat("hh:mm a");
        saveCurrenTime = currenTime.format(calFordTime.getTime());
        loadingBar = new ProgressDialog(this);


    }
    private void DisplayLastSeen(){
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("userState").hasChild("state")){
                            String state = snapshot.child("userState").child("state").getValue().toString();
                            String date = snapshot.child("userState").child("date").getValue().toString();
                            String time = snapshot.child("userState").child("time").getValue().toString();
                            if(state.equals("online")){
                                userLastseen.setText("Action");
                            }
                            else if(state.equals("offline")){
                                userLastseen.setText("Last Seen: "+date+" "+time);
                            }

                        }

                        else{
                            userLastseen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void SendMessage(){
        String messageText = MessageInputText.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this,"Please type a message",Toast.LENGTH_LONG).show();
        }
        else{
            String message_sender_ref = "Message/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref = "Message/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference user_message_key = RootRef.child("Message").child(messageSenderID).child(messageReceiverID).push();
            final String messagePushId = user_message_key.getKey();

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currendate =  new SimpleDateFormat("dd-MM-yyyy");
            saveCurrenDate = currendate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currenTime =  new SimpleDateFormat("HH:mm:aa");
            saveCurrenTime = currenTime.format(calFordTime.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageReceiverID);
            messageTextBody.put("messageID",messagePushId);
            messageTextBody.put("time",saveCurrenTime);
            messageTextBody.put("date",saveCurrenDate);



            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + messagePushId,messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + messagePushId,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                      
                    }
                    else {
                        String message = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });

        }

    }
}