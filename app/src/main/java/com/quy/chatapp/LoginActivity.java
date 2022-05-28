package com.quy.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quy.chatapp.databinding.ActivityLoginBinding;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    SharedPreferences sharedPreferences;
    DatabaseReference reference;
    boolean isPhoneTrue, isPasswordTrue = false;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = this.getSharedPreferences("Database", Context.MODE_PRIVATE);
        reference = FirebaseDatabase.getInstance().getReference();
        addEvent();
        textChangeEvent();
    }

    private void addEvent() {
        binding.textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = binding.etPhoneNumber.getText().toString().trim();
                if (phone.charAt(0) != '+') {
                    phone = "+84" + phone.substring(1);
                }
                String password = binding.etPassword.getText().toString().trim();
                reference.child("Users").child(phone).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.getResult().exists()) {
                            if (password.equals(task.getResult().child("password").getValue(String.class))) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("phoneNumber", phone);
                                editor.apply();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                return;
                            }
                        }
                        Toast.makeText(LoginActivity.this, "Incorrect phone number or password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void textChangeEvent() {
        binding.etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String validNumber = "^[+|0]{1}[0-9]{9,11}$";
                if (charSequence.toString().trim().matches(validNumber)) {
                    isPhoneTrue = true;
                } else {
                    isPhoneTrue = false;
                }
                checkInput();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().trim().length() > 5) {
                    isPasswordTrue = true;
                } else {
                    isPasswordTrue = false;
                }
                checkInput();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    private void checkInput() {
        if (isPhoneTrue && isPasswordTrue) {
            binding.btnSignIn.setEnabled(true);
            binding.btnSignIn.setBackgroundColor(Color.parseColor("#192497"));
        } else {
            binding.btnSignIn.setEnabled(false);
            binding.btnSignIn.setBackgroundColor(Color.parseColor("#50536C"));
        }
    }
}