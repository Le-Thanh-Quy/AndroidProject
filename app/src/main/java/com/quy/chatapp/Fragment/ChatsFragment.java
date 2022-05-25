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
import com.quy.chatapp.ModelView.ListRoom;
import com.quy.chatapp.ModelView.ListUserOnline;
import com.quy.chatapp.R;
import com.quy.chatapp.databinding.FragmentChatsBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChatsFragment extends Fragment {

    Context context;
    FragmentChatsBinding binding;

    public ChatsFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listUserOnlineController();
        listRoomChatController();
    }

    List<Room> listDataRoom;
    ListRoom listRoom;
    private void listRoomChatController() {
        listDataRoom = new ArrayList<Room>();
        listDataRoom = Arrays.asList(
                new Room("1", "hello tất cả mọi người mình là quý", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat"),
                new Room("1", "hello", "12-04-2022 12:00 AM", "null" , "Lê Thanh Quý", "chat")
        );
        listRoom = new ListRoom(listDataRoom, context);
        binding.listRoom.setHasFixedSize(true);
        binding.listRoom.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.listRoom.setAdapter(listRoom);
    }


    List<User> listDataUser;
    ListUserOnline listUserOnline;
    private void listUserOnlineController() {
        listDataUser = new ArrayList<User>();
        listDataUser = Arrays.asList(
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
        listUserOnline = new ListUserOnline(listDataUser, context);
        binding.listUserOnline.setHasFixedSize(true);
        binding.listUserOnline.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        binding.listUserOnline.setAdapter(listUserOnline);
    }
}