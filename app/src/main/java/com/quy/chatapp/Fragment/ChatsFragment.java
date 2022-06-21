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
import com.quy.chatapp.databinding.FragmentFriendBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatsFragment extends Fragment {

    private static DatabaseReference reference;
    private static Context context;
    private FragmentChatsBinding binding;
    private static String phone;

    public ChatsFragment() {
    }


    public static ChatsFragment getInstance(Context context1) {
        ChatsFragment instance = new ChatsFragment();
        context = context1;
        reference = FirebaseDatabase.getInstance().getReference();
        phone = User.getInstance().getPhoneNumber();
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
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


    public static ArrayList<Room> sortedListByTime(List<Room> rooms) {
        ArrayList<Room> sort = new ArrayList<>(rooms);
        Collections.sort(sort, new Comparator<Room>() {
                    public int compare(Room o1, Room o2) {
                        return o2.getRoomTimeLastMess().compareTo(o1.getRoomTimeLastMess());
                    }
                }
        );
        return sort;
    }

    private void addEventListenRoom() {
        reference.child("Users").child(phone).child("rooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<Room> rooms = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Room room = dataSnapshot.getValue(Room.class);
                    assert room != null;
                    if (room.getRoomType().equals("chat")) {
                        room.setUserId(dataSnapshot.getKey());
                    }
                    rooms.add(room);
                }
                try {
                    listDataRoom.clear();
                    listDataRoom.addAll(sortedListByTime(rooms));
                } catch (Exception e)
                {
                    System.out.println(e.toString());
                }


                binding.loadFragment.setVisibility(View.GONE);
                if (listDataRoom.isEmpty()) {
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
                User defaultUser = new User();
                defaultUser.setUserName("Tạo tin");
                listDataUser.add(defaultUser);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userPhoneNumber = dataSnapshot.getValue(String.class);
                    assert userPhoneNumber != null;
                    reference.child("Users").child(userPhoneNumber).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.getResult().getValue() != null) {
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