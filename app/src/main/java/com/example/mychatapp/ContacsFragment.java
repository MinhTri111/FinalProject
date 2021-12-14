package com.example.mychatapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContacsFragment extends Fragment {
    private View Contactsview;
    private RecyclerView myContactsList;
    private DatabaseReference ContactsRef,UserRef;
    private FirebaseAuth mAuth;
    private String CurrentUserId;




    public ContacsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Contactsview =  inflater.inflate(R.layout.fragment_contacs, container, false);
        myContactsList = (RecyclerView) Contactsview.findViewById(R.id.contact_list);
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        ContactsRef  = FirebaseDatabase.getInstance().getReference().child("Contacts").child(CurrentUserId);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return Contactsview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(ContactsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder,final int position, @NonNull Contacts model) {
                    String userIds = getRef(position).getKey();
                    System.out.println("Aaaaaaaaaaaaaaaaaa");
                    System.out.println(position);
                    UserRef.child(userIds).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                           if(snapshot.exists()){
                               if(snapshot.child("userState").hasChild("state")){
                                   String state = snapshot.child("userState").child("state").getValue().toString();
                                   String date = snapshot.child("userState").child("date").getValue().toString();
                                   String time = snapshot.child("userState").child("time").getValue().toString();
                                   if(state.equals("online")){
                                       holder.onlineIcon.setVisibility(View.VISIBLE);
                                   }
                                   else if(state.equals("offline")){
                                       holder.onlineIcon.setVisibility(View.INVISIBLE);
                                   }

                               }

                               else{
                                   holder.onlineIcon.setVisibility(View.INVISIBLE);
                               }

                               if(snapshot.hasChild("image")){
                                   String userImage = snapshot.child("image").getValue().toString();
                                   String profileName = snapshot.child("name").getValue().toString();
                                   String profileStatus = snapshot.child("status").getValue().toString();
                                   holder.username.setText(profileName);
                                   holder.userstatus.setText(profileStatus);
                                   Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                               }
                               else{
                                   String profileName = snapshot.child("name").getValue().toString();
                                   String profileStatus = snapshot.child("status").getValue().toString();
                                   holder.username.setText(profileName);
                                   holder.userstatus.setText(profileStatus);
                               }
                           }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
        TextView username, userstatus;
        CircleImageView profileImage;
        ImageView onlineIcon;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user_profile_name);
            userstatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_img);
            onlineIcon =(ImageView) itemView.findViewById(R.id.user_online_status);

        }
    }
}