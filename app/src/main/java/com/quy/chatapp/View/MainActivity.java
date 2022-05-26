package com.quy.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quy.chatapp.Fragment.ChatsFragment;
import com.quy.chatapp.Fragment.FriendFragment;
import com.quy.chatapp.Fragment.GroupFragment;
import com.quy.chatapp.R;
import com.quy.chatapp.Fragment.StatusFragment;
import com.quy.chatapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference reference;
    ActivityMainBinding binding;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(new ChatsFragment(MainActivity.this));
        reference = FirebaseDatabase.getInstance().getReference();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.action_chats:
                    fragment = new ChatsFragment(MainActivity.this);
                    loadFragment(fragment);
                    return true;
                case R.id.action_status:
                    fragment = new StatusFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.action_friends:
                    fragment = new FriendFragment(MainActivity.this);
                    loadFragment(fragment);
                    return true;
                case R.id.action_group:
                    fragment = new GroupFragment(MainActivity.this);
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}


