package com.quy.chatapp.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.quy.chatapp.Fragment.ChatsFragment;
import com.quy.chatapp.Fragment.FriendFragment;
import com.quy.chatapp.Fragment.GroupFragment;
import com.quy.chatapp.Model.MyToast;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.ModelView.ZoomableImageView;
import com.quy.chatapp.Notification.PushVoiceCall;
import com.quy.chatapp.R;
import com.quy.chatapp.Fragment.StatusFragment;
import com.quy.chatapp.databinding.ActivityMainBinding;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String id_user = "";
    private static boolean isCreateCallApi = true;
    private DatabaseReference reference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    ActivityMainBinding binding;
    SharedPreferences sharedPreferences;
    String phone;
    int countBack = 0;
    private String [] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_PHONE_STATE", "android.permission.SYSTEM_ALERT_WINDOW","android.permission.CAMERA"};
    private void offKeyboard() {
        if(getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        offKeyboard();
        int requestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission
                (MainActivity.this, android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE},
                    1);
        }
        sharedPreferences = this.getSharedPreferences("Database", Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            phone = sharedPreferences.getString("phoneNumber", "null");
            if (phone.equals("null")) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finishAndRemoveTask();
                return;
            }
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishAndRemoveTask();
            return;
        }
        User.getInstance().setPhoneNumber(phone);
        binding.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(ChatsFragment.getInstance(MainActivity.this));
        reference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        loadData();
        profileController();
        getToken();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("isNotification")) {
                if(bundle.getBoolean("isGroup")) {
                    String roomId = bundle.getString("room_id");
                    System.out.println(roomId + "AAAAAAAAAAAAAAAA");
                    Intent intent = new Intent(MainActivity.this, ChatGroupActivity.class);
                    intent.putExtra("id_room", roomId);
                    startActivity(intent);
                    return;
                } else {
                    User theirUser = (User) bundle.getSerializable("other_user");
                    System.out.println(theirUser.getPhoneNumber() + "AAAAAAAAAAAAAAAABBBBBB");
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("other_user", theirUser);
                    startActivity(intent);
                    return;
                }
            }
        }
        PushVoiceCall.user = User.getInstance();
        PushVoiceCall.context = MainActivity.this;
        PushVoiceCall.activity = MainActivity.this;
        if(isCreateCallApi) {
            isCreateCallApi = false;
            PushVoiceCall.addVoiceCallApi();
        }
    }


    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {

                            return;
                        }
                        String token = task.getResult();
                        reference.child("Users").child(phone).child("token").setValue(token);
                    }
                });
    }

    private void loadData() {
        reference.child("Users").child(phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    User.getInstance().setPassword(user.getPassword());
                    User.getInstance().setPhoneNumber(user.getPhoneNumber());
                    User.getInstance().setUserAvatar(user.getUserAvatar());
                    User.getInstance().setUserName(user.getUserName());

                    String avatar = User.getInstance().getUserAvatar();
                    assert avatar != null;
                    if (!avatar.equals("null")) {
                        try {
                            Glide.with(MainActivity.this)
                                    .load(avatar) // web image url
                                    .centerInside()
//                                .rotate(90)
                                    .error(R.drawable.profile)
                                    .placeholder(R.drawable.profile)
                                    .into(binding.avatar);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.action_chats:
                    countBack = 0;
                    fragment = ChatsFragment.getInstance(MainActivity.this);
                    loadFragment(fragment);
                    return true;
                case R.id.action_status:
                    countBack = 0;
                    fragment = StatusFragment.getInstance(MainActivity.this);
                    loadFragment(fragment);
                    return true;
                case R.id.action_friends:
                    countBack = 0;
                    fragment = FriendFragment.getInstance(MainActivity.this);
                    loadFragment(fragment);
                    return true;
                case R.id.action_group:
                    countBack = 0;
                    fragment = GroupFragment.getInstance(MainActivity.this);
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (countBack == 1) {
            onStop();
        } else {
            loadFragment(ChatsFragment.getInstance(MainActivity.this));
            MyToast.show(MainActivity.this, "Press again to exit", Toast.LENGTH_SHORT);
            countBack++;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        reference.child("Users").child(phone).child("status").child("is").setValue(false);
        reference.child("Users").child(phone).child("status").child("in").setValue(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }

    @Override
    protected void onResume() {
        PushVoiceCall.context = MainActivity.this;
        PushVoiceCall.activity = MainActivity.this;
        reference.child("Users").child(phone).child("status").child("is").setValue(true);
        super.onResume();
    }


    ImageView close_profile, user_avatar, edit_name, edit_pass, edit_avatar;
    EditText et_pass, et_phone, et_name;
    TextView user_name;
    CardView add_avatar;
    Button btn_logout;
    Dialog dialog;


    private void profileController() {
        binding.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (User.getInstance().getUserAvatar() != null) {
                    User user = User.getInstance();
                    dialog = new Dialog(MainActivity.this, R.style.Dialogxsmax);
                    dialog.setContentView(R.layout.profile_dialog);
                    close_profile = dialog.findViewById(R.id.close_profile);
                    user_avatar = dialog.findViewById(R.id.user_avatar);
                    edit_name = dialog.findViewById(R.id.edit_name);
                    edit_pass = dialog.findViewById(R.id.edit_pass);
                    et_pass = dialog.findViewById(R.id.et_pass);
                    et_phone = dialog.findViewById(R.id.et_phone);
                    et_name = dialog.findViewById(R.id.et_name);
                    user_name = dialog.findViewById(R.id.user_name);
                    add_avatar = dialog.findViewById(R.id.add_avatar);
                    btn_logout = dialog.findViewById(R.id.btn_logout);
                    edit_avatar = dialog.findViewById(R.id.edit_avatar);

                    et_name.setText(user.getUserName());
                    et_phone.setText(user.getPhoneNumber());
                    et_pass.setText(user.getPassword());
                    user_name.setText(user.getUserName());
                    if (!user.getUserAvatar().equals("null")) {

                        Picasso.get()
                                .load(user.getUserAvatar()) // web image url
                                .fit().centerInside()
//                                .rotate(90)
                                .error(R.drawable.profile)
                                .placeholder(R.drawable.profile)
                                .into(user_avatar);
                    }

                    addEventDialog(user);
                    addEventEdit(user);
                    try {
                        dialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void addEventEdit(User user) {
        edit_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogs, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:

                                ProgressDialog progress = new ProgressDialog(MainActivity.this);
                                progress.setTitle("Loading");
                                progress.setMessage("Updating...");
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
                                                        reference.child("Users").child(phone).child("userAvatar").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    edit_avatar.setVisibility(View.INVISIBLE);
                                                                    MyToast.show(MainActivity.this, "Change avatar successfully", Toast.LENGTH_SHORT);
                                                                    progress.dismiss();
                                                                } else {
                                                                    progress.dismiss();
                                                                    MyToast.show(MainActivity.this, "Error", Toast.LENGTH_SHORT);
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            } else {
                                                progress.dismiss();
                                                MyToast.show(MainActivity.this, "Error", Toast.LENGTH_SHORT);
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
                                                        reference.child("Users").child(phone).child("userAvatar").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    edit_avatar.setVisibility(View.INVISIBLE);
                                                                    MyToast.show(MainActivity.this, "Change avatar successfully", Toast.LENGTH_SHORT);
                                                                    progress.dismiss();
                                                                } else {
                                                                    progress.dismiss();
                                                                    MyToast.show(MainActivity.this, "Error", Toast.LENGTH_SHORT);
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            } else {
                                                progress.dismiss();
                                                MyToast.show(MainActivity.this, "Error", Toast.LENGTH_SHORT);
                                            }
                                        }
                                    });
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure to change?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        edit_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText input = new EditText(MainActivity.this);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                FrameLayout container = new FrameLayout(MainActivity.this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 50;
                params.rightMargin = 50;
                input.setLayoutParams(params);
                container.addView(input);
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Reset Password")
                        .setMessage("Enter your old password?")
                        .setView(container)
                        .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (input.getText().toString().trim().equals(user.getPassword())) {
                                    reference.child("Users").child(phone).child("password").setValue(et_pass.getText().toString().trim());
                                    edit_pass.setVisibility(View.INVISIBLE);
                                    MyToast.show(MainActivity.this, "Change password successfully", Toast.LENGTH_SHORT);
                                } else {
                                    MyToast.show(MainActivity.this, "Incorrect password", Toast.LENGTH_SHORT);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();

            }
        });

        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogs, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                reference.child("Users").child(phone).child("userName").setValue(et_name.getText().toString().trim());
                                edit_name.setVisibility(View.INVISIBLE);
                                user_name.setText(et_name.getText().toString().trim());
                                MyToast.show(MainActivity.this, "Change yor name successfully", Toast.LENGTH_SHORT);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure to change?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });


    }

    private void addEventDialog(User user) {
        close_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit_avatar.getVisibility() == View.VISIBLE
                        || edit_pass.getVisibility() == View.VISIBLE
                        || edit_name.getVisibility() == View.VISIBLE) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogs, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    dialog.dismiss();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("You haven't saved your changes,\n Are you sure you want to leave?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                } else {
                    dialog.dismiss();
                }

            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("phoneNumber", "null");
                editor.apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finishAndRemoveTask();
            }
        });

        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().equals(user.getUserName())
                        || charSequence.toString().trim().length() < 6) {
                    edit_name.setVisibility(View.INVISIBLE);
                } else {
                    edit_name.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().equals(user.getPassword())
                        || charSequence.toString().trim().length() < 6) {
                    edit_pass.setVisibility(View.INVISIBLE);
                } else {
                    edit_pass.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        add_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(MainActivity.this, R.style.Dialogs);
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

        user_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage(user_avatar);
            }
        });
    }

    private void showImage(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Dialog dialog_image = new Dialog(MainActivity.this, R.style.Dialogs);
        dialog_image.setContentView(R.layout.view_imge);
        ZoomableImageView mess_image = dialog_image.findViewById(R.id.mess_image);
        ImageView back_to_mess = dialog_image.findViewById(R.id.back_to_mess);

        mess_image.setImageBitmap(bitmap);
        back_to_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_image.dismiss();
            }
        });
        dialog_image.show();
    }

    Uri uri;
    byte[] bytes;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        edit_avatar.setVisibility(View.GONE);
        if (requestCode == 10) {
            if (data != null) {
                bytes = null;
                uri = data.getData();
                user_avatar.setImageURI(uri);
                edit_avatar.setVisibility(View.VISIBLE);
            }
        }
        if (requestCode == 100) {
            if (data != null) {
                if (data.getExtras() == null) return;
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                user_avatar.setImageBitmap(bitmap);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                bytes = byteArrayOutputStream.toByteArray();
                uri = null;
                edit_avatar.setVisibility(View.VISIBLE);
            }
        }
    }
}




