package com.quy.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.quy.chatapp.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    Button logout;
    ActivityRegisterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.overridePendingTransition(R.anim.animation_enter,
                R.anim.animation_leave);
        mAuth = FirebaseAuth.getInstance();
        addEvent();
    }

    private void addEvent() {
        binding.goBackLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndRemoveTask();
            }
        });
    }
}