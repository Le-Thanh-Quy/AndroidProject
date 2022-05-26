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
import com.quy.chatapp.R;
import com.quy.chatapp.databinding.FragmentFriendBinding;
import com.quy.chatapp.databinding.FragmentGroupBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupFragment extends Fragment {

    Context context;
    FragmentGroupBinding binding;

    public GroupFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGroupBinding.inflate(inflater, container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listFriendController();
    }

    List<Room> listData;
    ListRoom listRoom ;

    private void listFriendController() {
        listData = new ArrayList<Room>();
        listData = Arrays.asList(
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Nhóm chat vui vẻ", "group", "+84384933379", true),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "ABC", "group", "+84384933379", true),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Đồ án", "group", "+84384933379", true),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "19TCLC_DT4", "group", "+84384933379", true),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Haha", "group", "+84384933379", true)
        );
        listRoom = new ListRoom(listData, context);
        binding.listGroup.setHasFixedSize(true);
        binding.listGroup.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.listGroup.setAdapter(listRoom);
    }
}