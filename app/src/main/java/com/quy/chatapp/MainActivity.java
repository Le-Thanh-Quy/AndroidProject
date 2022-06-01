package com.quy.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.quy.chatapp.View.ChatActivity;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        System.out.println(User.getInstance().getPhoneNumber());


        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAndRemoveTask();
        return;
    }
}