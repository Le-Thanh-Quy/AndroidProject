package com.quy.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.quy.chatapp.databinding.ActivityRegisterBinding;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    String codeSent;
    ActivityRegisterBinding binding;
    boolean isPhoneTrue, isPasswordTrue, isRePasswordTrue = false;
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
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.closeVerificationCode.setVisibility(View.VISIBLE);
                sendVerificationCode();
            }
        });
        binding.closeVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.closeVerificationCode.setVisibility(View.GONE);
            }
        });
        textChangeEvent();
    }

    private void textChangeEvent() {
        binding.etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String validNumber = "^[+|0]{1}[0-9]{9,11}$";
                if(charSequence.toString().trim().matches(validNumber)) {
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

                if(charSequence.toString().trim().length() > 5) {
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
        binding.etRePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().equals(binding.etPassword.getText().toString().trim())) {
                    isRePasswordTrue = true;
                } else {
                    isRePasswordTrue = false;
                }
                checkInput();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.verificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 6) {
                    verifySignInCode();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void checkInput() {
        if(isPhoneTrue && isPasswordTrue && isRePasswordTrue) {
            binding.btnSignUp.setEnabled(true);
            binding.btnSignUp.setBackgroundColor(Color.parseColor("#192497"));
        } else {
            binding.btnSignUp.setEnabled(false);
            binding.btnSignUp.setBackgroundColor(Color.parseColor("#50536C"));
        }
    }

    private void verifySignInCode() {
        String code = binding.verificationCode.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //here you can open new activity
                            Toast.makeText(getApplicationContext(),
                                    "Login Successfull", Toast.LENGTH_LONG).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode() {
        String phone = binding.etPhoneNumber.getText().toString();
        if (phone.charAt(0) != '+') {
            phone = "+84" + phone.substring(1);
        }
        System.out.println("Phone: " + phone);
        PhoneAuthProvider.getInstance().verifyPhoneNumber
                (
                        phone,
                        120,
                        TimeUnit.SECONDS,
                        this,
                        mCallbacks
                );
    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
        }
    };
}