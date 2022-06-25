package com.quy.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.quy.chatapp.databinding.ActivityLoginBinding;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    SharedPreferences sharedPreferences;
    DatabaseReference reference;
    boolean isPhoneTrue, isPasswordTrue = false;
    String phone;
    ProgressDialog progress;
    FirebaseAuth mAuth;
    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = this.getSharedPreferences("Database", Context.MODE_PRIVATE);
        reference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        addEvent();
        textChangeEvent();
    }

    boolean isShowPass = false;

    private void addEvent() {
        binding.hideShowPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowPass) {
                    binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.hideShowPass.setImageDrawable(ContextCompat.getDrawable(LoginActivity.this , R.drawable.show_pass));
                } else {
                    binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.hideShowPass.setImageDrawable(ContextCompat.getDrawable(LoginActivity.this, R.drawable.hidden_pass));
                }
                binding.etPassword.setSelection(binding.etPassword.getText().length());
                isShowPass = !isShowPass;
            }
        });
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
                        MyToast.show(LoginActivity.this, "Incorrect phone number or password",  Toast.LENGTH_SHORT);
                    }
                });
            }
        });

        binding.forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.closeForgotPass.setVisibility(View.VISIBLE);
                binding.forgotPhone.setText("");
                binding.verificationCode.setText("");
                binding.etResetPass.setText("");
                binding.verificationCode.setVisibility(View.GONE);
                binding.etResetPass.setVisibility(View.GONE);
                binding.findForgotPhone.setEnabled(true);
                binding.verificationCode.setEnabled(true);
                binding.forgotPhone.setEnabled(true);
            }
        });
        binding.closeForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.closeForgotPass.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        binding.findForgotPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String forgot_phone = binding.forgotPhone.getText().toString().trim();
                if (forgot_phone.charAt(0) != '+') {
                    forgot_phone = "+84" + forgot_phone.substring(1);
                }
                String finalForgot_phone = forgot_phone;
                reference.child("Users").child(forgot_phone).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.getResult().exists()) {
                            progress = new ProgressDialog(LoginActivity.this);
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
                                            .setPhoneNumber(finalForgot_phone)
                                            .setTimeout(120L, TimeUnit.SECONDS)
                                            .setActivity(LoginActivity.this)
                                            .setCallbacks(mCallbacks)
                                            .build();
                            PhoneAuthProvider.verifyPhoneNumber(options);
                        } else {
                            MyToast.show(LoginActivity.this, "Incorrect phone number", Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        });

        binding.resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress = new ProgressDialog(LoginActivity.this);
                progress.setTitle("Loading");
                progress.setMessage("Please wait...");
                progress.setCancelable(false);
                try {
                    progress.show();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                String forgot_phone = binding.forgotPhone.getText().toString().trim();
                if (forgot_phone.charAt(0) != '+') {
                    forgot_phone = "+84" + forgot_phone.substring(1);
                }
                String newPassword = binding.etResetPass.getText().toString().trim();
                reference.child("Users").child(forgot_phone).child("password").setValue(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progress.dismiss();
                        MyToast.show(LoginActivity.this, "Change password successfully", Toast.LENGTH_SHORT);
                        binding.closeForgotPass.setVisibility(View.GONE);
                    }
                });

            }
        });
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
                            binding.etResetPass.setVisibility(View.VISIBLE);
                            binding.etResetPass.requestFocus();
                            binding.verificationCode.setEnabled(false);
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                MyToast.show(getApplicationContext(),
                                        "Incorrect Verification Code", Toast.LENGTH_LONG);
                            }
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
            binding.findForgotPhone.setEnabled(false);
            binding.forgotPhone.setEnabled(false);
            binding.verificationCode.setVisibility(View.VISIBLE);
            binding.verificationCode.requestFocus();
            progress.dismiss();
        }
    };

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

        binding.forgotPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String validNumber = "^[+|0]{1}[0-9]{9,11}$";
                if (charSequence.toString().trim().matches(validNumber)) {
                    binding.findForgotPhone.setVisibility(View.VISIBLE);
                } else {
                    binding.findForgotPhone.setVisibility(View.GONE);
                }
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
        binding.etResetPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() > 5) {
                    binding.resetPass.setVisibility(View.VISIBLE);
                } else {
                    binding.resetPass.setVisibility(View.GONE);
                }
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