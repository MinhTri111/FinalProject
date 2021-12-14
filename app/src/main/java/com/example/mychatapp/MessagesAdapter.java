package com.example.mychatapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    public MessagesAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout,parent,false);
        mAuth = FirebaseAuth.getInstance();

        return new MessagesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);
        String fromUserID = messages.getFrom();
        String fromMessagesType = messages.getType();
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("image")){
                    String receiverImage= snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImagel);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverProfileImagel.setVisibility(View.GONE);
        holder.SenderMessageText.setVisibility(View.GONE);
        holder.ReceiverMessageText.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        if(fromMessagesType.equals("text")){


            if(fromUserID.equals(messageSenderID)){

                holder.SenderMessageText.setVisibility(View.VISIBLE);
                holder.SenderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.SenderMessageText.setTextColor(Color.WHITE);
                holder.SenderMessageText.setText(messages.getMessage() +"\n\n"+ messages.getTime()+" - "+messages.getDate());

            }
            else{

                holder.ReceiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImagel.setVisibility(View.VISIBLE);
                holder.ReceiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.ReceiverMessageText.setTextColor(Color.WHITE);
                holder.ReceiverMessageText.setText(messages.getMessage() +"\n\n"+ messages.getTime()+" - "+messages.getDate());
            }
        }  else if(fromMessagesType.equals("image")){
            if(fromUserID.equals(messageSenderID))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

            }
            else{
                holder.receiverProfileImagel.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);

            }
        }

    }

    @Override
    public int getItemCount() {
       return userMessagesList.size();
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {
        public TextView SenderMessageText, ReceiverMessageText;
        public CircleImageView receiverProfileImagel;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            SenderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            ReceiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImagel = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
          messageReceiverPicture = (ImageView) itemView.findViewById(R.id.message_receiver_image);
          messageSenderPicture = (ImageView) itemView.findViewById(R.id.message_sender_image);
        }
    }

}
