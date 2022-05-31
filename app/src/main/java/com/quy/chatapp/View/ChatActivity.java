package com.quy.chatapp.View;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;
import com.quy.chatapp.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference reference;
    ActivityChatBinding binding;
    User other_user;
    String phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.overridePendingTransition(R.anim.animation_enter,
                R.anim.animation_leave);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            other_user = (User) bundle.getSerializable("other_user");
        }
        reference = FirebaseDatabase.getInstance().getReference();
        phone = User.getInstance().getPhoneNumber();
        binding.phone.setText(other_user.getPhoneNumber());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
    @Override
    protected void onPause() {
        reference.child("Users").child(phone).child("status").setValue(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        reference.child("Users").child(phone).child("status").setValue(true);
        super.onResume();
    }
}