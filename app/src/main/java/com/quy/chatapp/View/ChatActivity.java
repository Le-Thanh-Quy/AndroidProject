package com.quy.chatapp.View;

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

import com.quy.chatapp.R;
import com.quy.chatapp.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uiEvent();

    }

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
}