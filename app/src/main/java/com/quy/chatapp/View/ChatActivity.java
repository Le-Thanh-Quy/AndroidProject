package com.quy.chatapp.View;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.quy.chatapp.R;
import com.quy.chatapp.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}