package com.quy.chatapp.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quy.chatapp.Model.Room;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.ModelView.ListFriend;
import com.quy.chatapp.ModelView.ListRoom;
import com.quy.chatapp.ModelView.ListUserOnline;
import com.quy.chatapp.R;
import com.quy.chatapp.databinding.FragmentChatsBinding;
import com.quy.chatapp.databinding.FragmentFriendBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendFragment extends Fragment {

    Context context;
    FragmentFriendBinding binding;

    public FriendFragment(Context context) {
        this.context = context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFriendBinding.inflate(inflater, container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listFriendController();
    }

    List<User> listData;
    ListFriend listFriend;

    private void listFriendController() {
        listData = new ArrayList<User>();
        listData = Arrays.asList(
                new User("1", "Lê Thanh Quý", "null"),
                new User("1", "Trần Nguyễn Anh Trình", "null"),
                new User("1", "Thanh Quý", "null"),
                new User("1", "Lê Quý", "null"),
                new User("1", "Lê", "null"),
                new User("1", "Quý", "null"),
                new User("1", "Thanh", "null"),
                new User("1", "Lê Quý Thanh", "null"),
                new User("1", "Thanh Quý Lê", "null"),
                new User("1", "Lê Thanh Quý", "null"),
                new User("1", "Lê Thanh Quý", "null"),
                new User("1", "Lê Thanh Quý", "null")
        );
        listFriend = new ListFriend(listData, context);
        binding.listFriend.setHasFixedSize(true);
        binding.listFriend.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.listFriend.setAdapter(listFriend);
    }
}