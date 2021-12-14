package com.example.mychatapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPaper;
    private TabLayout mytabLayout;
    private TabAccessorAdapter mytabAccessorAdapter;


    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ZALO BK");

        myViewPaper = (ViewPager) findViewById(R.id.main_tabs_paper);
        mytabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());
        myViewPaper.setAdapter(mytabAccessorAdapter);

        mytabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mytabLayout.setupWithViewPager(myViewPaper);

        mAuth = FirebaseAuth.getInstance();

        RootRef = FirebaseDatabase.getInstance().getReference();



    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            SendUserToLoginActivity();
        }
        else{
            updateUserStatus("online");
            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null){
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null){
            updateUserStatus("offline");
        }

    }

    private void VerifyUserExistance() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.child("name").exists())){


                }
                else{
                    SendUserToSettingActivity();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToFindFriendActivity() {
        Intent findFriendIntent = new Intent(MainActivity.this, FindFriendActivity.class);
        startActivity(findFriendIntent);

    }
    private void SendUserToSettingActivity() {
        Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingIntent);

    }
    private void updateUserStatus(String state){
        String saveCurrentTime,saveCurrentDate;
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currendate =  new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currendate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currenTime =  new SimpleDateFormat("HH:mm a");
        saveCurrentTime = currenTime.format(calFordTime.getTime());

        HashMap<String, Object> onlinestatus = new HashMap<>();
        onlinestatus.put("time",saveCurrentTime);
        onlinestatus.put("date",saveCurrentDate);
        onlinestatus.put("state",state);
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlinestatus);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.options_menu,menu);
         return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId() == R.id.main_logout_option){
             updateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
         }
        if(item.getItemId() == R.id.main_settings_option){
            SendUserToSettingActivity();

        }
        if(item.getItemId() == R.id.main_find_friend_option){
            SendUserToFindFriendActivity();
        }
        if(item.getItemId() == R.id.main_create_group_option){
            RequestNewGroup();

        }

        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Nhâp tên group: ");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("Tri cute");
        builder.setView(groupNameField);
        builder.setPositiveButton("Tạo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    String groupName = groupNameField.getText().toString();
                    if(TextUtils.isEmpty(groupName)){
                        Toast.makeText(MainActivity.this,"Hãy nhập tên nhóm",Toast.LENGTH_SHORT).show();

                    }
                    else{
                        CreateNewGroup(groupName);

                    }
            }
        });
        builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();


    }

    private void CreateNewGroup(String groupName) {
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"Tạo nhóm thành công!",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}