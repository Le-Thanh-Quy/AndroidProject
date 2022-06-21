package com.quy.chatapp.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.quy.chatapp.Model.MyToast;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;
import com.quy.chatapp.databinding.ActivityCompleteRegisterBinding;

import java.io.ByteArrayOutputStream;

public class CompleteRegisterActivity extends AppCompatActivity {

    ActivityCompleteRegisterBinding binding;
    String phone;
    String password;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompleteRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            phone = bundle.getString("phone");
            password = bundle.getString("password");
        }
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        reference = firebaseDatabase.getReference();
        sharedPreferences = this.getSharedPreferences("Database",Context.MODE_PRIVATE);
        changePage();
        signUp();
        addAvatar();
    }

    private void addAvatar() {
        binding.addAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(CompleteRegisterActivity.this, R.style.Dialogs);
                dialog.setContentView(R.layout.layout_option_image);
                TextView takePhoto;
                TextView chooseFromGallery;
                TextView cancel;
                takePhoto = dialog.findViewById(R.id.takePhoto);
                chooseFromGallery = dialog.findViewById(R.id.chooseFromGallery);
                cancel = dialog.findViewById(R.id.cancel_option);


                dialog.show();
                takePhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, 100);
                        dialog.dismiss();
                    }
                });
                chooseFromGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, 10);
                        dialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    Uri uri;
    byte[] bytes;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10) {
            if (data != null) {
                bytes = null;
                uri = data.getData();
                binding.imgAvatar.setImageURI(uri);
                binding.imgAvatar.setVisibility(View.VISIBLE);
            }
        }
        if (requestCode == 100) {
            if (data != null) {
                if (data.getExtras() == null) return;
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                binding.imgAvatar.setImageBitmap(bitmap);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                bytes = byteArrayOutputStream.toByteArray();
                binding.imgAvatar.setVisibility(View.VISIBLE);
                uri = null;
            }
        }
    }

    private void signUp() {
        binding.completeRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = binding.etUserName.getText().toString().trim();
                if (userName.isEmpty()) {
                    MyToast.show(CompleteRegisterActivity.this, "What's your name??", Toast.LENGTH_SHORT);
                } else {
                    ProgressDialog progress = new ProgressDialog(CompleteRegisterActivity.this);
                    progress.setTitle("Loading");
                    progress.setMessage("Signing up...");
                    progress.setCancelable(false);
                    try {
                        progress.show();
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }

                    if (uri != null) {
                        storageReference = firebaseStorage.getReference().child("Users").child(phone).child("avatar");
                        storageReference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            User user = new User();
                                            user.setPhoneNumber(phone);
                                            user.setPassword(password);
                                            user.setUserName(userName);
                                            user.setUserAvatar(uri.toString());
                                            reference.child("Users").child(phone).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.putString("phoneNumber", phone);
                                                        editor.apply();
                                                        MyToast.show(CompleteRegisterActivity.this, "Welcome to Chatapp!", Toast.LENGTH_SHORT);
                                                        Intent intent = new Intent(CompleteRegisterActivity.this, MainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        progress.dismiss();
                                                    } else {
                                                        progress.dismiss();
                                                        MyToast.show(CompleteRegisterActivity.this, "Error", Toast.LENGTH_SHORT);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    progress.dismiss();
                                    MyToast.show(CompleteRegisterActivity.this, "Error", Toast.LENGTH_SHORT);
                                }
                            }
                        });
                    } else if (bytes != null) {
                        storageReference = firebaseStorage.getReference().child("Users").child(phone).child("avatar");
                        storageReference.putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            User user = new User();
                                            user.setPhoneNumber(phone);
                                            user.setPassword(password);
                                            user.setUserName(userName);
                                            user.setUserAvatar(uri.toString());
                                            reference.child("Users").child(phone).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.putString("phoneNumber", phone);
                                                        editor.apply();
                                                        MyToast.show(CompleteRegisterActivity.this, "Welcome to Chatapp!", Toast.LENGTH_SHORT);
                                                        Intent intent = new Intent(CompleteRegisterActivity.this, MainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        progress.dismiss();
                                                    } else {
                                                        progress.dismiss();
                                                        MyToast.show(CompleteRegisterActivity.this, "Error", Toast.LENGTH_SHORT);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    progress.dismiss();
                                    MyToast.show(CompleteRegisterActivity.this, "Error", Toast.LENGTH_SHORT);
                                }
                            }
                        });
                    } else {
                        User user = new User();
                        user.setPhoneNumber(phone);
                        user.setPassword(password);
                        user.setUserName(userName);
                        user.setUserAvatar("null");

                        reference.child("Users").child(phone).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("phoneNumber", phone);
                                    editor.apply();
                                    MyToast.show(CompleteRegisterActivity.this, "Welcome to Chatapp!", Toast.LENGTH_SHORT);
                                    Intent intent = new Intent(CompleteRegisterActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    progress.dismiss();
                                } else {
                                    progress.dismiss();
                                    MyToast.show(CompleteRegisterActivity.this, "Error", Toast.LENGTH_SHORT);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void changePage() {
        binding.layoutName.setVisibility(View.VISIBLE);
        binding.addName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.layoutName.animate()
                        .translationX(-binding.layoutName.getWidth())
                        .alpha(0.0f)
                        .setDuration(400)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                binding.layoutName.setVisibility(View.GONE);
                            }
                        });

            }
        });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.layoutName.setVisibility(View.VISIBLE);
                binding.layoutName.animate()
                        .translationXBy(-binding.layoutName.getWidth())
                        .translationX(0)
                        .alpha(1.0f)
                        .setDuration(400)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);

                            }
                        });
            }
        });
    }
}