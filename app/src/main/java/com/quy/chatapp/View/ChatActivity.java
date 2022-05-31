package com.quy.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.chatapp.R;
import com.quy.chatapp.User;
import com.quy.chatapp.databinding.ActivityChatBinding;
import com.squareup.picasso.Picasso;

public class ChatActivity extends AppCompatActivity {

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (v instanceof EditText) {
            Rect outRect = new Rect();
            v.getGlobalVisibleRect(outRect);
            if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
        return super.dispatchTouchEvent(ev);

    }

    ActivityChatBinding binding;
    DatabaseReference reference;
    User user;
    String sendPhone = "+84384933378";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        uiEvent();
        loadPage();
        reference = FirebaseDatabase.getInstance().getReference();
        user = User.getInstance();
    }

    private void uiEvent() {
        binding.inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    binding.chooseImageSend.setVisibility(View.GONE);
                    binding.inputMessage.setHint("Nhập tin nhắn...");
                } else {
                    binding.chooseImageSend.setVisibility(View.VISIBLE);
                    binding.inputMessage.setHint("Aaa...");
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

    }
    private void loadPage() {
        reference.child("Users").child(sendPhone).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                User receive_user = task.getResult().getValue(User.class);
                assert receive_user != null;
                binding.textName.setText(receive_user.getUserName());
                Picasso.get().load(user.getUserAvatar()).into(binding.avatar);
            }
        });

        reference.child("Users").child(sendPhone).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isOnline = snapshot.getValue(Boolean.class);
                if(isOnline) {

                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}