package com.quy.chatapp.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.quy.chatapp.Model.Mess;
import com.quy.chatapp.Model.MyToast;
import com.quy.chatapp.Model.Room;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.ModelView.ListChat;
import com.quy.chatapp.ModelView.ZoomableImageView;
import com.quy.chatapp.Notification.PushVoiceCall;
import com.quy.chatapp.Notification.SendNotification;
import com.quy.chatapp.R;
import com.quy.chatapp.databinding.ActivityChatBinding;
import com.quy.chatapp.databinding.ActivityChatGroupBinding;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatGroupActivity extends AppCompatActivity {

    private void offKeyboard() {
        binding.inputMessage.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.inputMessage.getWindowToken(), 0);
    }

    ActivityChatGroupBinding binding;
    DatabaseReference reference;
    boolean isFirstMess = false;
    User user;
    User theirUser;
    Room chat_room;
    List<Mess> listData;
    ListChat listChat;
    Bitmap bitmapAvatar;
    boolean isSeen;
    boolean isOpenSeen;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    boolean isNotification = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        reference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        isSeen = true;
        isOpenSeen = true;
        this.overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            theirUser = (User) bundle.getSerializable("other_user");
            isNotification = bundle.getBoolean("isNotification");
        }

        binding.sendIcon.setColorFilter(Color.parseColor("#192497"));
        user = User.getInstance();
        if (user == null) {
            SharedPreferences sharedPreferences;
            sharedPreferences = this.getSharedPreferences("Database", Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                user.setPhoneNumber(sharedPreferences.getString("phoneNumber", "null"));
                if (user.getPhoneNumber().equals("null")) {
                    Intent intent = new Intent(ChatGroupActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishAndRemoveTask();
                    return;
                }
            } else {
                Intent intent = new Intent(ChatGroupActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finishAndRemoveTask();
                return;
            }
        }
        chat_room = new Room();
        listData = new ArrayList<>();

        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.getResult().exists()) {
                    Room room = task.getResult().getValue(Room.class);
                    assert room != null;
                    chat_room.setRoomID(room.getRoomID());
                    chat_room.setRoomName(room.getRoomName());
                } else {
                    binding.notFound.setVisibility(View.VISIBLE);
                    isFirstMess = true;
                }

                uiEvent();
                loadPage();
                sendMessEvent();
                sendIconEvent();
            }
        });

    }

    @Override
    public void finish() {
        isSeen = false;
        reference.removeEventListener(valueEventListener);
        this.overridePendingTransition(0, 0);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isNotification) {
            Intent intent = new Intent(ChatGroupActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        MainActivity.id_user = "";
        finishAndRemoveTask();
    }

    @Override
    protected void onPause() {
        MainActivity.id_user = "";
        reference.child("Users").child(user.getPhoneNumber()).child("status").child("is").setValue(false);
        reference.child("Users").child(user.getPhoneNumber()).child("status").child("in").setValue(String.valueOf(System.currentTimeMillis()));
        isSeen = false;

        super.onPause();
    }

    @Override
    protected void onResume() {
        MainActivity.id_user = theirUser.phoneNumber;
        isSeen = true;
        reference.child("Users").child(user.getPhoneNumber()).child("status").child("is").setValue(true);
        super.onResume();
    }


    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (isSeen) {
                listData.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Mess mess = dataSnapshot.getValue(Mess.class);
                    listData.add(mess);
                }
                if (listData.isEmpty()) {
                    binding.notFound.setVisibility(View.VISIBLE);
                } else {
                    binding.notFound.setVisibility(View.GONE);
                    reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("in").setValue(listData.get(listData.size() - 1).getTime());
                    reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("is").setValue(true);
                }
                binding.progressBar.setVisibility(View.GONE);

                listChat.notifyDataSetChanged();
                if (!isOpenSeen) {
                    if (listData.get(listData.size() - 1).getUserId().equals(user.getPhoneNumber())) {
                        binding.listChat.scrollToPosition(listChat.getItemCount() - 1);
                        binding.newMess.setVisibility(View.GONE);
                    } else {
                        if (!listData.get(listData.size() - 1).getType().equals("notification")) {
                            binding.newMess.setVisibility(View.VISIBLE);
                        }
                    }
                }
                isOpenSeen = false;

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void eventChat() {
        listChat = new ListChat(listData, ChatGroupActivity.this, user.getPhoneNumber(), false, theirUser, chat_room.getRoomID(), bitmapAvatar);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        binding.listChat.setLayoutManager(layoutManager);
        binding.listChat.setNestedScrollingEnabled(false);
        binding.listChat.setAdapter(listChat);

        reference.child("Rooms").child(chat_room.getRoomID()).child("listMess").addValueEventListener(valueEventListener);
        reference.child("Rooms").child(chat_room.getRoomID()).child("iconId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String iconId = snapshot.getValue(String.class);
                    chat_room.setIconId(iconId);
                    int idHinh = getResources().getIdentifier("like_mess_" + iconId, "drawable", getPackageName());
                    assert iconId != null;
                    if (Integer.parseInt(iconId) != 0) {
                        binding.sendIcon.setColorFilter(null);
                    } else {
                        binding.sendIcon.setColorFilter(Color.parseColor("#192497"));
                    }
                    binding.sendIcon.setImageResource(idHinh);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPage() {
        if (chat_room.getRoomName() != null) {
            binding.textName.setText(chat_room.getRoomName());
        } else {
            binding.textName.setText(theirUser.getUserName());
        }

        if (!theirUser.getUserAvatar().equals("null")) {
            Picasso.get().load(theirUser.getUserAvatar()).fit().centerCrop().placeholder(ChatGroupActivity.this.getResources().getDrawable(R.drawable.profile)).into(binding.avatar, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    BitmapDrawable drawable = (BitmapDrawable) binding.avatar.getDrawable();
                    bitmapAvatar = drawable.getBitmap();
                    if (chat_room.getRoomID() != null) {
                        eventChat();
                    }
                }

                @Override
                public void onError(Exception e) {

                }
            });
        } else {
            BitmapDrawable drawable = (BitmapDrawable) binding.avatar.getDrawable();
            bitmapAvatar = drawable.getBitmap();
        }

        reference.child("Users").child(theirUser.getPhoneNumber()).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Boolean isOnline = snapshot.child("is").getValue(Boolean.class);
                    if (!isOnline) {
                        binding.userOnline.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#979797")));
                        String time = snapshot.child("in").getValue(String.class);
                        ;
                        long lassMessTime = Long.parseLong(time);
                        long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lassMessTime);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lassMessTime);
                        if (minutes == 0) {
                            binding.textOnline.setText("Vừa truy cập");
                        } else if (minutes < 60) {
                            binding.textOnline.setText("Hoạt động " + minutes + " phút trước");
                        } else if (hours < 24) {
                            binding.textOnline.setText("Hoạt động " + hours + " giờ trước");
                        } else if (hours < 24 * 5 + 1) {
                            binding.textOnline.setText("Hoạt động " + hours / 24 + " ngày trước");
                        } else {
                            binding.textOnline.setText("Đang ngoại tuyến");
                        }
                    } else {
                        binding.userOnline.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5FD364")));
                        binding.textOnline.setText("Đang hoạt động");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.child("Users").child(user.getPhoneNumber()).child("friends").child(theirUser.getPhoneNumber()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.layoutAddFriend.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutAddFriend.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.voiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFirstMess) {
                    MyToast.show(ChatGroupActivity.this, "Bạn chưa thể thực hiện cuộc gọi", 0);
                } else {
                    reference.child("Users").child(theirUser.getPhoneNumber()).child("isCall").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            reference.child("Users").child(user.getPhoneNumber()).child("isCall").setValue(user.getPhoneNumber());
                            if (task.getResult().exists()) {
                                MyToast.show(ChatGroupActivity.this, theirUser.getUserName() + " đang thực hiện cuộc gọi", 0);
                                reference.child("Users").child(user.getPhoneNumber()).child("isCall").setValue(null);
                            } else {
                                reference.child("Users").child(theirUser.getPhoneNumber()).child("isCall").setValue(user.getPhoneNumber());
                                Intent intent = new Intent(ChatGroupActivity.this, VoiceCallActivity.class);
                                intent.putExtra("user", user);
                                intent.putExtra("theirUser", theirUser);
                                startActivityForResult(intent, 110011);
                            }
                        }
                    });

                }
            }
        });
        PushVoiceCall.context = ChatGroupActivity.this;
        PushVoiceCall.activity = ChatGroupActivity.this;
    }


    private void uiEvent() {
        binding.inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    binding.chooseImageSend.setVisibility(View.GONE);
                    binding.inputMessage.setHint("Nhập tin nhắn...");
                    binding.inputMessage.setMaxLines(10);
                } else {
                    binding.chooseImageSend.setVisibility(View.VISIBLE);
                    binding.inputMessage.setHint("Aaa...");
                    binding.inputMessage.setMaxLines(1);

                }
            }
        });

        binding.inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    binding.sendIcon.setVisibility(View.GONE);
                    binding.sendMess.setVisibility(View.VISIBLE);
                } else {
                    binding.sendIcon.setVisibility(View.VISIBLE);
                    binding.sendMess.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.listChat.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                offKeyboard();
                return false;
            }
        });

        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNotification) {
                    Intent intent = new Intent(ChatGroupActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                MainActivity.id_user = "";
                finishAndRemoveTask();
            }
        });
        binding.newMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.newMess.setVisibility(View.GONE);
                binding.listChat.scrollToPosition(listChat.getItemCount() - 1);
            }
        });

        binding.listChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
                    binding.newMess.setVisibility(View.GONE);
                }
            }
        });

        binding.userInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFirstMess) {
                    MyToast.show(ChatGroupActivity.this, "Bạn chưa tạo tin nhắn với người này", Toast.LENGTH_SHORT);
                } else {
                    openUserInfo();
                }

            }
        });

        binding.chooseImageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(ChatGroupActivity.this, R.style.Dialogs);
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
                        startActivityForResult(intent, 101);
                        dialog.dismiss();
                    }
                });
                chooseFromGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(Intent.createChooser(new Intent()
                                .setAction(Intent.ACTION_GET_CONTENT)
                                .setType("image/* video/*"), "pickFile"), 11);
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
        binding.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child("Users").child(user.getPhoneNumber()).child("friends").child(theirUser.getPhoneNumber()).setValue(theirUser.getPhoneNumber());
            }
        });
    }

    ImageView close_chat_info, user_avatar, edit_name, icon_chat;
    EditText et_name;
    TextView user_name;
    Dialog dialog;
    RelativeLayout layout_icon;
    CardView layout_call, layout_video;

    void openUserInfo() {
        dialog = new Dialog(ChatGroupActivity.this, R.style.Dialogs);
        dialog.setContentView(R.layout.chat_info);
        close_chat_info = dialog.findViewById(R.id.close_chat_info);
        user_avatar = dialog.findViewById(R.id.user_avatar);
        edit_name = dialog.findViewById(R.id.edit_name);
        et_name = dialog.findViewById(R.id.et_name);
        user_name = dialog.findViewById(R.id.user_name);
        icon_chat = dialog.findViewById(R.id.icon_chat);
        layout_icon = dialog.findViewById(R.id.layout_icon);
        layout_call = dialog.findViewById(R.id.layout_call);

        et_name.setText(chat_room.getRoomName());
        user_name.setText(theirUser.getUserName());
        if (chat_room.getIconId() != null) {
            int idIcon = this.getResources().getIdentifier("like_mess_" + chat_room.getIconId(), "drawable", this.getPackageName());
            icon_chat.setImageResource(idIcon);
            if (chat_room.getIconId().equals("0")) {
                icon_chat.setColorFilter(Color.parseColor("#192497"));
            }
        } else {
            icon_chat.setColorFilter(Color.parseColor("#192497"));
        }

        if (!theirUser.getUserAvatar().equals("null")) {

            Picasso.get()
                    .load(theirUser.getUserAvatar()) // web image url
                    .fit().centerInside()
                    .error(R.drawable.profile)
                    .placeholder(R.drawable.profile)
                    .into(user_avatar);
        }

        addEventDialog();
        dialog.show();
    }

    private void addEventDialog() {
        close_chat_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().equals(chat_room.getRoomName())) {
                    edit_name.setVisibility(View.INVISIBLE);
                } else {
                    edit_name.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = et_name.getText().toString();
                reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("roomName").setValue(content);
                String time_now = String.valueOf(System.currentTimeMillis());
                Mess mess = new Mess();
                mess.setType("notification");
                mess.setUserId(user.getPhoneNumber());
                mess.setTime(time_now);
                mess.setMessage("1__@__" + content);
                reference.child("Rooms").child(chat_room.getRoomID()).child("listMess").child(time_now).setValue(mess);
                binding.textName.setText(content);
                chat_room.setRoomName(content);
                edit_name.setVisibility(View.INVISIBLE);
            }
        });

        layout_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(ChatGroupActivity.this, ChooseIcon.class), 1100);
            }
        });

        layout_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFirstMess) {
                    MyToast.show(ChatGroupActivity.this, "Bạn chưa thể thực hiện cuộc gọi", 0);
                } else {
                    reference.child("Users").child(theirUser.getPhoneNumber()).child("isCall").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            reference.child("Users").child(user.getPhoneNumber()).child("isCall").setValue(user.getPhoneNumber());
                            if (task.getResult().exists()) {
                                MyToast.show(ChatGroupActivity.this, theirUser.getUserName() + " đang thực hiện cuộc gọi", 0);
                                reference.child("Users").child(user.getPhoneNumber()).child("isCall").setValue(null);
                            } else {
                                reference.child("Users").child(theirUser.getPhoneNumber()).child("isCall").setValue(user.getPhoneNumber());
                                Intent intent = new Intent(ChatGroupActivity.this, VoiceCallActivity.class);
                                intent.putExtra("user", user);
                                intent.putExtra("theirUser", theirUser);
                                startActivityForResult(intent, 110011);
                            }
                        }
                    });

                }
            }
        });
    }

    Uri uri;
    byte[] bytes;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1100 && resultCode == RESULT_OK && data != null) {
            String idResult = String.valueOf(data.getExtras().getInt("icon"));
            int idHinh = getResources().getIdentifier("like_mess_" + idResult, "drawable", getPackageName());
            if (Integer.parseInt(idResult) != 0) {
                icon_chat.setColorFilter(null);
            } else {
                icon_chat.setColorFilter(Color.parseColor("#192497"));
            }
            icon_chat.setImageResource(idHinh);
            reference.child("Rooms").child(chat_room.getRoomID()).child("iconId").setValue(idResult);
            String time_now = String.valueOf(System.currentTimeMillis());
            Mess mess = new Mess();
            mess.setType("notification");
            mess.setUserId(user.getPhoneNumber());
            mess.setTime(time_now);
            mess.setMessage("2__@__" + idResult);
            reference.child("Rooms").child(chat_room.getRoomID()).child("listMess").child(time_now).setValue(mess);

        } else if (requestCode == 1101 && resultCode == RESULT_OK && data != null) {
            sendIcon(String.valueOf(data.getExtras().getInt("icon")), false);
        } else if (requestCode == 11 && resultCode == RESULT_OK && data != null) {
            showImageVideo(data);
        } else if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            showImage(data);
        } else if (requestCode == 110011 && data != null) {
            String status = data.getExtras().getString("status");
            sendMess(status, "call");
        }
    }


    private void showImage(Intent data) {
        Dialog dialog_image = new Dialog(ChatGroupActivity.this, R.style.Dialogs);
        dialog_image.setContentView(R.layout.view_page);
        ZoomableImageView mess_image = dialog_image.findViewById(R.id.mess_image);
        if (data.getExtras() == null) return;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        bytes = byteArrayOutputStream.toByteArray();
        uri = null;
        mess_image.setVisibility(View.VISIBLE);
        mess_image.setImageBitmap(bitmap);
        isImage = true;

        ImageView back_to_mess = dialog_image.findViewById(R.id.back_to_mess);
        EditText content_file_mess = dialog_image.findViewById(R.id.content_file_mess);
        ImageView send_file_mess = dialog_image.findViewById(R.id.send_file_mess);

        back_to_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_image.dismiss();
            }
        });

        send_file_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog progress = new ProgressDialog(ChatGroupActivity.this);
                progress.setTitle("Loading");
                progress.setMessage("Sending...");
                progress.setCancelable(false);
                try {
                    progress.show();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                if (bytes != null) {
                    storageReference = firebaseStorage.getReference().child("Chats").child(user.getPhoneNumber()).child(String.valueOf(System.currentTimeMillis()));
                    storageReference.putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        sendFile(uri.toString(), isImage);
                                        String mess_content = content_file_mess.getText().toString().trim();
                                        if (!mess_content.isEmpty()) {
                                            sendMess(mess_content, "text");
                                        }
                                        progress.dismiss();
                                        dialog_image.dismiss();
                                    }
                                });
                            } else {
                                progress.dismiss();
                                dialog_image.dismiss();
                                MyToast.show(ChatGroupActivity.this, "Error", Toast.LENGTH_SHORT);
                            }
                        }
                    });
                }
            }
        });
        dialog_image.show();
    }

    boolean isImage = true;

    private void showImageVideo(Intent data) {
        uri = data.getData();
        Dialog dialog_image = new Dialog(ChatGroupActivity.this, R.style.Dialogs);
        dialog_image.setContentView(R.layout.view_page);
        ZoomableImageView mess_image = dialog_image.findViewById(R.id.mess_image);
        ImageView back_to_mess = dialog_image.findViewById(R.id.back_to_mess);
        VideoView mess_video = dialog_image.findViewById(R.id.mess_video);
        RelativeLayout layout_mess_video = dialog_image.findViewById(R.id.layout_mess_video);
        EditText content_file_mess = dialog_image.findViewById(R.id.content_file_mess);
        ImageView send_file_mess = dialog_image.findViewById(R.id.send_file_mess);


        back_to_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_image.dismiss();
            }
        });

        send_file_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog progress = new ProgressDialog(ChatGroupActivity.this);
                progress.setTitle("Loading");
                progress.setMessage("Sending...");
                progress.setCancelable(false);
                try {
                    progress.show();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

                if (uri != null) {
                    storageReference = firebaseStorage.getReference().child("Chats").child(user.getPhoneNumber()).child(String.valueOf(System.currentTimeMillis()));
                    storageReference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        sendFile(uri.toString(), isImage);
                                        String mess_content = content_file_mess.getText().toString().trim();
                                        if (!mess_content.isEmpty()) {
                                            sendMess(mess_content, "text");
                                        }
                                        progress.dismiss();
                                        dialog_image.dismiss();
                                    }
                                });
                            } else {
                                progress.dismiss();
                                dialog_image.dismiss();
                                MyToast.show(ChatGroupActivity.this, "Error", Toast.LENGTH_SHORT);
                            }
                        }
                    });
                }
            }
        });

        if (uri.toString().contains("image")) {
            isImage = true;
            bytes = null;
            uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mess_image.setVisibility(View.VISIBLE);
                mess_image.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            isImage = false;
            bytes = null;
            if (uri != null) {
                layout_mess_video.setVisibility(View.VISIBLE);
                mess_video.setVisibility(View.VISIBLE);
                MediaController mc = new MediaController(ChatGroupActivity.this) {
                    @Override
                    public void hide() {
                        this.show();
                    }
                };
                mess_video.setMediaController(mc);
                mc.setAnchorView(mess_video);
                ((ViewGroup) mc.getParent()).removeView(mc);
                ((FrameLayout) dialog_image.findViewById(R.id.videoViewWrapper)).setVisibility(View.VISIBLE);
                ((FrameLayout) dialog_image.findViewById(R.id.videoViewWrapper)).addView(mc);
                mc.setVisibility(View.VISIBLE);
                mess_video.setVideoURI(uri);
                mess_video.seekTo(0);
                mess_video.pause();
            }
        }
        dialog_image.show();
    }

    private void sendFile(String fileUrl, boolean isImage) {
        String time_now = String.valueOf(System.currentTimeMillis());
        Mess mess = new Mess();
        mess.setMessage(fileUrl);
        mess.setTime(time_now);
        mess.setUserId(user.getPhoneNumber());
        if (isImage) {
            mess.setType("image");
        } else {
            mess.setType("video");
        }

        if (isFirstMess) {
            Room room = new Room();
            room.setRoomType("chat");
            room.setRoomID(time_now);
            room.setRoomName(theirUser.getUserName());
            reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).setValue(room);
            room.setRoomName(user.getUserName());
            reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).setValue(room);

            if (isImage) {
                firstMess(room, "Đã gửi một ảnh", time_now, mess);
            } else {
                firstMess(room, "Đã gửi một video", time_now, mess);
            }

            isFirstMess = false;
            chat_room.setRoomID(time_now);
            eventChat();
        } else {
            if (isImage) {
                SendNotification.send(ChatGroupActivity.this, theirUser.getToken(), user.getUserName(), mess.getMessage(), user.getPhoneNumber(), mess.getType(), user.getUserAvatar(), false);
                nextMess(time_now, "Đã gửi một ảnh", mess);
            } else {
                SendNotification.send(ChatGroupActivity.this, theirUser.getToken(), user.getUserName(), "Đã gửi một video", user.getPhoneNumber(), mess.getType(), user.getUserAvatar(), false);
                nextMess(time_now, "Đã gửi một video", mess);
            }

        }
        offKeyboard();
    }


    private void sendIconEvent() {
        binding.sendIcon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivityForResult(new Intent(ChatGroupActivity.this, ChooseIcon.class), 1101);
                return false;
            }
        });

        binding.sendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendIcon("0", true);
            }
        });
    }

    private void sendIcon(String id, boolean usingIconRoom) {
        String iconId = id;
        if (usingIconRoom) {
            if (chat_room.getIconId() != null) {
                iconId = chat_room.getIconId();
            }
        }
        String time_now = String.valueOf(System.currentTimeMillis());
        Mess mess = new Mess();
        mess.setMessage(iconId);
        mess.setTime(time_now);
        mess.setUserId(user.getPhoneNumber());
        mess.setType("icon");
        if (isFirstMess) {
            Room room = new Room();
            room.setRoomType("chat");
            room.setRoomID(time_now);
            room.setRoomName(theirUser.getUserName());
            reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).setValue(room);
            room.setRoomName(user.getUserName());
            reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).setValue(room);

            firstMess(room, "Đã gửi một biểu cảm", time_now, mess);
            isFirstMess = false;
            chat_room.setRoomID(time_now);
            eventChat();
        } else {
            SendNotification.send(ChatGroupActivity.this, theirUser.getToken(), user.getUserName(), "Đã gửi một biểu cảm", user.getPhoneNumber(), mess.getType(), user.getUserAvatar(), false);
            nextMess(time_now, "Đã gửi một biểu cảm", mess);
        }
        offKeyboard();

    }


    private void sendMessEvent() {
        binding.sendMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mess_content = binding.inputMessage.getText().toString().trim();
                if (!mess_content.isEmpty()) {
                    sendMess(mess_content, "text");
                }

            }
        });
    }

    private void sendMess(String mess_content, String type) {
        String time_now = String.valueOf(System.currentTimeMillis());
        Mess mess = new Mess();
        mess.setMessage(mess_content);
        mess.setTime(time_now);
        mess.setUserId(user.getPhoneNumber());
        mess.setType(type);
        if (isFirstMess) {
            Room room = new Room();
            room.setRoomType("chat");
            room.setRoomID(time_now);
            room.setRoomName(theirUser.getUserName());
            reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).setValue(room);
            room.setRoomName(user.getUserName());
            reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).setValue(room);

            firstMess(room, mess_content, time_now, mess);

            isFirstMess = false;
            chat_room.setRoomID(time_now);
            eventChat();
        } else {
            SendNotification.send(ChatGroupActivity.this, theirUser.getToken(), user.getUserName(), mess_content, user.getPhoneNumber(), mess.getType(), user.getUserAvatar(), false);
            nextMess(time_now, mess_content, mess);
        }
        binding.inputMessage.setText("");
        offKeyboard();
    }

    private void firstMess(Room room, String mess_content, String time_now, Mess mess) {
//        reference.child("Rooms").child(room.getRoomID()).child("lastMess").setValue(mess_content);
//        reference.child("Rooms").child(room.getRoomID()).child("roomTimeLastMess").setValue(time_now);
        reference.child("Rooms").child(room.getRoomID()).child("listMess").child(time_now).setValue(mess);
        reference.child("Rooms").child(room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("is").setValue(true);
        reference.child("Rooms").child(room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("in").setValue(time_now);
        reference.child("Rooms").child(room.getRoomID()).child("listSeen").child(theirUser.getPhoneNumber()).child("is").setValue(false);
        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).child("roomTimeLastMess").setValue(time_now);
        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("roomTimeLastMess").setValue(time_now);
        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).child("lastMess").setValue(mess_content);
        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("lastMess").setValue(mess_content);
        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).child("lastMessId").setValue(user.getPhoneNumber());
        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("lastMessId").setValue(user.getPhoneNumber());
    }

    private void nextMess(String time_now, String mess_content, Mess mess) {
//        reference.child("Rooms").child(chat_room.getRoomID()).child("lastMess").setValue(mess_content);
//        reference.child("Rooms").child(chat_room.getRoomID()).child("roomTimeLastMess").setValue(time_now);
        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).child("roomTimeLastMess").setValue(time_now);
        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("roomTimeLastMess").setValue(time_now);
        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).child("lastMess").setValue(mess_content);
        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("lastMess").setValue(mess_content);
        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).child("lastMessId").setValue(user.getPhoneNumber());
        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("lastMessId").setValue(user.getPhoneNumber());
        reference.child("Rooms").child(chat_room.getRoomID()).child("listMess").child(time_now).setValue(mess);
        reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("is").setValue(true);
        reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("in").setValue(time_now);
        reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(theirUser.getPhoneNumber()).child("is").setValue(false);
    }
}