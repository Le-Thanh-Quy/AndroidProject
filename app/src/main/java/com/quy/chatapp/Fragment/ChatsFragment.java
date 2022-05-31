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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    private DatabaseReference reference;
    Context context;
    FragmentChatsBinding binding;
    String phone;

    public ChatsFragment(Context context) {
        this.context = context;
        phone = User.getInstance().getPhoneNumber();
        reference = FirebaseDatabase.getInstance().getReference();
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
        listRoom = new ListRoom(listDataRoom, context);
        binding.listRoom.setHasFixedSize(true);
        binding.listRoom.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.listRoom.setAdapter(listRoom);
        addEventListenRoom();
    }

    private void addEventListenRoom() {
        reference.child("Users").child(phone).child("rooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listDataRoom.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Room room = dataSnapshot.getValue(Room.class);
                    listDataRoom.add(room);
                }
                binding.loadFragment.setVisibility(View.GONE);
                if(listDataRoom.isEmpty()) {
                    binding.notFound.setVisibility(View.VISIBLE);
                } else {
                    binding.notFound.setVisibility(View.GONE);
                }
                listRoom.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    List<User> listDataUser;
    ListUserOnline listUserOnline;
    private void listUserOnlineController() {
        listDataUser = new ArrayList<User>();
        listUserOnline = new ListUserOnline(listDataUser, context);
        binding.listUserOnline.setHasFixedSize(true);
        binding.listUserOnline.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        binding.listUserOnline.setAdapter(listUserOnline);
        addEventListenFriend();
    }

    private void addEventListenFriend() {
        reference.child("Users").child(phone).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listDataUser.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String userPhoneNumber = dataSnapshot.getValue(String.class);
                    assert userPhoneNumber != null;
                    reference.child("Users").child(userPhoneNumber).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.getResult().getValue() != null) {
                                User user = task.getResult().getValue(User.class);
                                assert user != null;
                                listDataUser.add(user);
                                listUserOnline.notifyDataSetChanged();
                            }
                        }
                    });
                }
                listUserOnline.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}