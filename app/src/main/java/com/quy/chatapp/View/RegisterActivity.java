package com.quy.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quy.chatapp.Model.MyToast;
import com.quy.chatapp.R;
import com.quy.chatapp.databinding.ActivityRegisterBinding;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    DatabaseReference database;
    String codeSent;
    ActivityRegisterBinding binding;
    boolean isPhoneTrue, isPasswordTrue, isRePasswordTrue = false;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.overridePendingTransition(R.anim.animation_enter,
                R.anim.animation_leave);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        addEvent();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    boolean isShowPass = false;
    boolean isShowRePass = false;

    private void addEvent() {
        binding.hideShowPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowPass) {
                    binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.hideShowPass.setImageDrawable(ContextCompat.getDrawable(RegisterActivity.this, R.drawable.show_pass));
                } else {
                    binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.hideShowPass.setImageDrawable(ContextCompat.getDrawable(RegisterActivity.this, R.drawable.hidden_pass));
                }
                binding.etPassword.setSelection(binding.etPassword.getText().length());
                isShowPass = !isShowPass;
            }
        });
        binding.hideShowRePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowRePass) {
                    binding.etRePassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.hideShowRePass.setImageDrawable(ContextCompat.getDrawable(RegisterActivity.this, R.drawable.show_pass));
                } else {
                    binding.etRePassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.hideShowRePass.setImageDrawable(ContextCompat.getDrawable(RegisterActivity.this, R.drawable.hidden_pass));
                }
                binding.etRePassword.setSelection(binding.etRePassword.getText().length());
                isShowRePass = !isShowRePass;
            }
        });
        binding.goBackLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode();
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
        binding.etRePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals(binding.etPassword.getText().toString().trim())) {
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
                if (charSequence.length() == 6) {
                    verifySignInCode();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void checkInput() {
        if (isPhoneTrue && isPasswordTrue && isRePasswordTrue) {
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
                            Intent intent = new Intent(RegisterActivity.this, CompleteRegisterActivity.class);
                            intent.putExtra("phone", phone);
                            intent.putExtra("password", binding.etPassword.getText().toString().trim());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(RegisterActivity.this.getCurrentFocus().getWindowToken(), 0);
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                MyToast.show(getApplicationContext(),
                                        "Incorrect Verification Code", Toast.LENGTH_LONG);
                            }
                        }
                    }
                });
    }

    String phone;

    private void sendVerificationCode() {
        phone = binding.etPhoneNumber.getText().toString();
        if (phone.charAt(0) != '+') {
            phone = "+84" + phone.substring(1);
        }
        database.child("Users").child(phone).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.getResult().exists()) {
                    MyToast.show(RegisterActivity.this, "Phone number already", Toast.LENGTH_SHORT);
                } else {
                    progress = new ProgressDialog(RegisterActivity.this);
                    progress.setTitle("Loading");
                    progress.setMessage("Please wait...");
                    progress.setCancelable(false);
                    try {
                        progress.show();
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phone)
                                    .setTimeout(120L, TimeUnit.SECONDS)
                                    .setActivity(RegisterActivity.this)
                                    .setCallbacks(mCallbacks)
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
//                    PhoneAuthProvider.getInstance().verifyPhoneNumber
//                            (
//                                    phone,
//                                    120,
//                                    TimeUnit.SECONDS,
//                                    RegisterActivity.this,
//                                    mCallbacks
//                            );
                }
            }
        });

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
            binding.closeVerificationCode.setVisibility(View.VISIBLE);
            progress.dismiss();
        }
    };
}