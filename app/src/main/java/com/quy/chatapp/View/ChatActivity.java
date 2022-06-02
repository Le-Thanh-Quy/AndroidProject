package com.quy.chatapp.View;

//<<<<<<< HEAD
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.quy.chatapp.Model.User;
//import com.quy.chatapp.R;
//import com.quy.chatapp.databinding.ActivityChatBinding;
//
//public class ChatActivity extends AppCompatActivity {
//
//    private DatabaseReference reference;
//    ActivityChatBinding binding;
//    User other_user;
//    String phone;
//=======

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Lists;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.chatapp.Model.Mess;
import com.quy.chatapp.Model.Room;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.ModelView.ListChat;
import com.quy.chatapp.R;
import com.quy.chatapp.databinding.ActivityChatBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        View v = getCurrentFocus();
//        if (v instanceof EditText) {
//            Rect outRect = new Rect();
//            v.getGlobalVisibleRect(outRect);
//            if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
//                v.clearFocus();
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//            }
//        }
//        return super.dispatchTouchEvent(ev);
//
//    }

    private void offKeyboard() {
        binding.inputMessage.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.inputMessage.getWindowToken(), 0);
    }

    ActivityChatBinding binding;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        isSeen = true;
        isOpenSeen = true;
        this.overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            theirUser = (User) bundle.getSerializable("other_user");
        }
        reference = FirebaseDatabase.getInstance().getReference();
        user = User.getInstance();
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
                    isFirstMess = true;
                }
                uiEvent();
                loadPage();
                sendMess();
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
        finishAndRemoveTask();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        reference.child("Users").child(user.getPhoneNumber()).child("status").setValue(false);
        isSeen = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        isSeen = true;
        reference.child("Users").child(user.getPhoneNumber()).child("status").setValue(true);
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
                    binding.progressBar.setVisibility(View.VISIBLE);
                } else {
                    reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("in").setValue(listData.get(listData.size() - 1).getTime());
                    reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("is").setValue(true);
                }
                binding.progressBar.setVisibility(View.GONE);

                listChat.notifyDataSetChanged();
                if(!isOpenSeen) {
                    if (listData.get(listData.size() - 1).getUserId().equals(user.getPhoneNumber())) {
                        binding.listChat.scrollToPosition(listChat.getItemCount() - 1);
                        binding.newMess.setVisibility(View.GONE);
                    } else {
                        binding.newMess.setVisibility(View.VISIBLE);
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
        listChat = new ListChat(listData, ChatActivity.this, user.getPhoneNumber(), false, theirUser, chat_room.getRoomID(), bitmapAvatar);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        binding.listChat.setLayoutManager(layoutManager);
        binding.listChat.setNestedScrollingEnabled(false);
        binding.listChat.setAdapter(listChat);

        reference.child("Rooms").child(chat_room.getRoomID()).child("listMess").addValueEventListener(valueEventListener);
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
                if(!recyclerView.canScrollVertically(1)) {
                    binding.newMess.setVisibility(View.GONE);
                }
            }
        });
    }


    private int getCurrentItem() {
        return ((LinearLayoutManager) binding.listChat.getLayoutManager())
                .findFirstVisibleItemPosition();
    }

    private void loadPage() {
        binding.textName.setText(theirUser.getUserName());
        if (!theirUser.getUserAvatar().equals("null")) {
            Picasso.get().load(theirUser.getUserAvatar()).fit().centerCrop().placeholder(ChatActivity.this.getResources().getDrawable(R.drawable.profile)).into(binding.avatar, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    BitmapDrawable drawable = (BitmapDrawable) binding.avatar.getDrawable();
                    bitmapAvatar = drawable.getBitmap();
                    eventChat();
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
                    Boolean isOnline = snapshot.getValue(Boolean.class);
                    if (!isOnline) {
                        binding.userOnline.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#979797")));
                        binding.textOnline.setText("Đang ngoại tuyến");
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
    }

    private void sendMess() {
        binding.sendMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mess_content = binding.inputMessage.getText().toString().trim();
                if (!mess_content.isEmpty()) {
                    String time_now = String.valueOf(System.currentTimeMillis());
                    Mess mess = new Mess();
                    mess.setMessage(mess_content);
                    mess.setTime(time_now);
                    mess.setUserId(user.getPhoneNumber());
                    mess.setType("text");
                    if (isFirstMess) {
                        Room room = new Room();
                        room.setRoomType("chat");
                        room.setRoomID(time_now);
                        room.setRoomName(theirUser.getUserName());
                        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).setValue(room);
                        room.setRoomName(user.getUserName());
                        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).setValue(room);

                        reference.child("Rooms").child(room.getRoomID()).child("lastMess").setValue(mess_content);
                        reference.child("Rooms").child(room.getRoomID()).child("roomTimeLastMess").setValue(time_now);
                        reference.child("Rooms").child(room.getRoomID()).child("listMess").child(time_now).setValue(mess);
                        reference.child("Rooms").child(room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).setValue(true);
                        reference.child("Rooms").child(room.getRoomID()).child("listSeen").child(theirUser.getPhoneNumber()).setValue(false);
                        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).child("roomTimeLastMess").setValue(time_now);
                        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("roomTimeLastMess").setValue(time_now);
                        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).child("lastMess").setValue(mess_content);
                        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("lastMess").setValue(mess_content);
                        isFirstMess = false;
                        chat_room.setRoomID(time_now);
                        eventChat();
                    } else {
                        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).child("roomTimeLastMess").setValue(time_now);
                        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("roomTimeLastMess").setValue(time_now);
                        reference.child("Users").child(theirUser.getPhoneNumber()).child("rooms").child(user.getPhoneNumber()).child("lastMess").setValue(mess_content);
                        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(theirUser.getPhoneNumber()).child("lastMess").setValue(mess_content);
                        reference.child("Rooms").child(chat_room.getRoomID()).child("listMess").child(time_now).setValue(mess);
                        reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("is").setValue(true);
                        reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("in").setValue(time_now);
                        reference.child("Rooms").child(chat_room.getRoomID()).child("listSeen").child(theirUser.getPhoneNumber()).child("is").setValue(false);
                    }
                }
                binding.inputMessage.setText("");
                offKeyboard();
            }
        });
    }
}