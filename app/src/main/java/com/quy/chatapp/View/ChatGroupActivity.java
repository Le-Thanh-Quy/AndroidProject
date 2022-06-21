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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
import com.quy.chatapp.ModelView.ListFriendGroup;
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
    User user;
    String roomID;
    Room chat_room;
    List<Mess> listData;
    ListChat listChat;
    Bitmap bitmapAvatar;
    boolean isSeen;
    boolean isOpenSeen;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    boolean isNotification = false;
    ArrayList<String> listUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        reference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        isSeen = true;
        isOpenSeen = true;
        listUser = new ArrayList<>();
        this.overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            roomID = bundle.getString("id_room");
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

        reference.child("Rooms").child(roomID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.getResult().exists()) {
                    Room room = task.getResult().getValue(Room.class);
                    assert room != null;
                    chat_room.setRoomID(roomID);
                    chat_room.setRoomName(room.getRoomName());
                    chat_room.setImageRoom(room.getImageRoom());
                    reference.child("Rooms").child(roomID).child("listSeen").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                                String phoneNumber = dataSnapshot.getKey();
                                listUser.add(phoneNumber);
                            }
                            uiEvent();
                            loadPage();
                            eventChat();
                            sendMessEvent();
                            sendIconEvent();
                            binding.avatar.setImageDrawable(getDrawable(R.drawable.team));
                            binding.userOnline.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Intent intent = new Intent(ChatGroupActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishAndRemoveTask();
                    return;
                }

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
        MainActivity.id_user = roomID;
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
        listChat = new ListChat(listData, ChatGroupActivity.this, user.getPhoneNumber(), true, new User(), chat_room.getRoomID(), bitmapAvatar);
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
        binding.textName.setText(chat_room.getRoomName());
        if (!chat_room.getImageRoom().equals("null")) {
            Glide.with(ChatGroupActivity.this).load(chat_room.getImageRoom()).centerCrop().placeholder(ChatGroupActivity.this.getResources().getDrawable(R.drawable.team)).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    BitmapDrawable drawable = (BitmapDrawable) binding.avatar.getDrawable();
                    bitmapAvatar = drawable.getBitmap();
                    return false;
                }
            }).into(binding.avatar);
        } else {
            BitmapDrawable drawable = (BitmapDrawable) binding.avatar.getDrawable();
            bitmapAvatar = drawable.getBitmap();
        }

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
                openChatInfo();
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
    }

    ImageView close_chat_info, user_avatar, edit_name, icon_chat;
    EditText et_name;
    TextView user_name;
    Dialog dialog;
    RelativeLayout layout_icon;
    CardView layout_call, layout_video, add_user;

    void openChatInfo() {
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
        add_user = dialog.findViewById(R.id.add_user);

        add_user.setVisibility(View.VISIBLE);
        TextView textView = dialog.findViewById(R.id.title_name);
        textView.setText("Tên nhóm:");

        user_avatar.setImageDrawable(getDrawable(R.drawable.team));
        et_name.setText(chat_room.getRoomName());
        user_name.setText(chat_room.getRoomName());
        if (chat_room.getIconId() != null) {
            int idIcon = this.getResources().getIdentifier("like_mess_" + chat_room.getIconId(), "drawable", this.getPackageName());
            icon_chat.setImageResource(idIcon);
            if (chat_room.getIconId().equals("0")) {
                icon_chat.setColorFilter(Color.parseColor("#192497"));
            }
        } else {
            icon_chat.setColorFilter(Color.parseColor("#192497"));
        }

        if (!chat_room.getImageRoom().equals("null")) {

            Picasso.get()
                    .load(chat_room.getImageRoom()) // web image url
                    .fit().centerInside()
                    .error(R.drawable.team)
                    .placeholder(R.drawable.team)
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
                reference.child("Rooms").child(roomID).child("roomName").setValue(content);
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

        add_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogAddFriend();
            }
        });
    }


    private void openDialogAddFriend() {
        final Dialog dialog = new Dialog(ChatGroupActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog_add_friend);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView avatar_result = dialog.findViewById(R.id.avatar_result);
        EditText phone_number = dialog.findViewById(R.id.phone_number);
        TextView name_result = dialog.findViewById(R.id.name_result);
        CardView yes_add_friend = dialog.findViewById(R.id.yes_add_friend);
        CardView no_add_friend = dialog.findViewById(R.id.no_add_friend);
        RelativeLayout result_search = dialog.findViewById(R.id.result_search);

        phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String validNumber = "^[+|0]{1}[0-9]{9,11}$";
                String search_phone = charSequence.toString().trim();
                if (search_phone.length() > 0) {
                    if (search_phone.charAt(0) != '+') {
                        search_phone = "+84" + search_phone.substring(1);
                    }
                }
                if (search_phone.matches(validNumber)) {
                    reference.child("Users").child(search_phone).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.getResult().exists()) {
                                User user = task.getResult().getValue(User.class);
                                name_result.setText(user.getUserName());
                                if (!"null".equals(user.getUserAvatar())) {
                                    Glide.with(ChatGroupActivity.this)
                                            .load(user.getUserAvatar())
                                            .centerInside()
//                                            .rotate(90)
                                            .error(R.drawable.profile)
                                            .placeholder(R.drawable.profile)
                                            .into(avatar_result);
                                } else {
                                    avatar_result.setImageDrawable(ChatGroupActivity.this.getDrawable(R.drawable.profile));
                                }
                                result_search.setVisibility(View.VISIBLE);
                            } else {
                                result_search.setVisibility(View.GONE);
                            }
                        }
                    });

                } else {
                    result_search.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        no_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        yes_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String search_phone = phone_number.getText().toString().trim();
                if (search_phone.length() > 0) {
                    if (search_phone.charAt(0) != '+') {
                        search_phone = "+84" + search_phone.substring(1);
                    }
                }
                String finalSearch_phone = search_phone;
                reference.child("Rooms").child(roomID).child("listSeen").child(search_phone).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.getResult().exists()) {
                            MyToast.show(ChatGroupActivity.this, "Người này hiện đang ở trong nhóm", 0);
                        } else {
                            String time = String.valueOf(System.currentTimeMillis());
                            Mess mess = new Mess();
                            mess.setMessage("4__@__" + finalSearch_phone);
                            mess.setTime(time);
                            mess.setType("notification");
                            mess.setUserId(user.getPhoneNumber());
                            reference.child("Rooms").child(roomID).child("listMess").child(time).setValue(mess);
                            reference.child("Rooms").child(roomID).child("listSeen").child(user.getPhoneNumber()).child("is").setValue(true);
                            Room room = new Room();
                            room.setRoomID(roomID);
                            room.setRoomType("group");
                            room.setLastMess(User.getInstance().getUserName() + " đã thêm thành viên");
                            room.setLastMessId(user.getPhoneNumber());
                            room.setRoomTimeLastMess(time);
                            reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(roomID).setValue(room);
                            listUser.add(finalSearch_phone);
                            for (String user_phone : listUser) {
                                reference.child("Rooms").child(roomID).child("listSeen").child(user_phone).child("is").setValue(false);
                                reference.child("Rooms").child(roomID).child("roomName").setValue(binding.textName.getText().toString().trim());
                                reference.child("Rooms").child(roomID).child("imageRoom").setValue("null");
                                reference.child("Users").child(user_phone).child("rooms").child(roomID).setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
        dialog.show();
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


        if (isImage) {
            nextMess(time_now, "Đã gửi một ảnh", mess);
        } else {
            nextMess(time_now, "Đã gửi một video", mess);
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
        nextMess(time_now, "Đã gửi một biểu cảm", mess);
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
        nextMess(time_now, mess_content, mess);
        binding.inputMessage.setText("");
        offKeyboard();
    }

    private void nextMess(String time_now, String mess_content, Mess mess) {
        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(roomID).child("roomTimeLastMess").setValue(time_now);
        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(roomID).child("lastMess").setValue(mess_content);
        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(roomID).child("lastMessId").setValue(user.getPhoneNumber());

        reference.child("Rooms").child(chat_room.getRoomID()).child("listMess").child(time_now).setValue(mess);
        reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("is").setValue(true);


        for (String str : listUser) {
            reference.child("Users").child(str).child("rooms").child(roomID).child("roomTimeLastMess").setValue(time_now);
            reference.child("Users").child(str).child("rooms").child(roomID).child("lastMess").setValue(mess_content);
            reference.child("Users").child(str).child("rooms").child(roomID).child("lastMessId").setValue(user.getPhoneNumber());
            reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(str).child("is").setValue(false);
        }
    }
}