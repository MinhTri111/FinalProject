package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.load.engine.Initializable;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountButton;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int GalleryPick = 1;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private String urlimage="";
    private androidx.appcompat.widget.Toolbar SettingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        InitializeFields();
        userName.setVisibility(View.INVISIBLE);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");
        RetrieveUserInfo();
        UpdateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpadateSettings();
            }
        });
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });
    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("name")) &&(snapshot.hasChild("image"))  ){
                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveUserStatus = snapshot.child("status").getValue().toString();
                    String retrieveUserProfileImage = snapshot.child("image").getValue().toString();
                    
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                    Picasso.get().load(retrieveUserProfileImage).into(userProfileImage);


                }
                else if((snapshot.exists()) && (snapshot.hasChild("name"))){
                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveUserStatus = snapshot.child("status").getValue().toString();


                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                }
                else{
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Xin hãy cài đặt tài khoản!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void UpadateSettings() {
        String setUserName= userName.getText().toString();
        String setStatus = userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this,"Xin hãy nhập tên của bạn!",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)) {

            Toast.makeText(this, "Xin hãy nhập trạng thái của bạn!", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
             RootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                 @Override
                 public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Cài đặt tài khoản thành công!", Toast.LENGTH_SHORT).show();
                            sendToMainActivity();
                        }
                        else{
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();


                        }
                 }
             });

        }
    }
    private void sendToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void InitializeFields() {
        UpdateAccountButton = (Button) findViewById(R.id.update_setting_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
        SettingToolbar = (Toolbar)findViewById(R.id.setting_toolbar);
        setSupportActionBar(SettingToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Cài đặt tài khoản ");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GalleryPick && resultCode == RESULT_OK && data !=null){
            Uri ImageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Tải ảnh đại diện");
                loadingBar.setMessage("Please wait...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resulturi = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
                UploadTask uploadTask = (UploadTask) filePath.putFile(resulturi).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString ();
                                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                                System.out.println(downloadUrl);
                                urlimage = downloadUrl;
                                RootRef.child ("Users").child(currentUserID).child("image").setValue(downloadUrl)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText (SettingsActivity.this, "Ảnh hồ sơ được lưu trữ thành công vào cơ sở dữ liệu firebase.", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss ();

                                    }
                                    else{
                                        String message = task.getException (). getMessage ();
                                        Toast.makeText (SettingsActivity.this, "Đã xảy ra lỗi ..." + message, Toast.LENGTH_SHORT) .show ();
                                        loadingBar.dismiss ();

                                    }
                                }
                            });
                                RootRef.child ("Users").child(currentUserID).child("image").setValue(downloadUrl);
                            }
                        });
                    }

                /*Task<Uri> urltask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                       if(!task.isSuccessful()){
                           throw task.getException();
                       }
                       return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Toast.makeText(SettingsActivity.this, "Thay đổi ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                            if (downloadUri != null) {

                                String downloadUrl = downloadUri.toString(); //YOU WILL GET THE DOWNLOAD URL HERE !!!!
                                RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        loadingBar.dismiss();
                                        if(!task.isSuccessful()){
                                            String error=task.getException().toString();
                                            Toast.makeText(SettingsActivity.this,"Error : "+error,Toast.LENGTH_LONG).show();
                                        }else{

                                        }
                                    }
                                });
                            }

                        } else {
                            // Handle failures
                            // ...
                            Toast.makeText(SettingsActivity.this,"Error",Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }

                    }
                });*/


                });

        }

    }
}
}
