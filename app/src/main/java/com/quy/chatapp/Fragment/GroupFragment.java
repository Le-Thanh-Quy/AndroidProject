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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    private DatabaseReference reference;
    Context context;
    FragmentGroupBinding binding;
    String phone;

    public GroupFragment(Context context) {
        this.context = context;
        reference = FirebaseDatabase.getInstance().getReference();
        phone = User.getInstance().getPhoneNumber();
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
        listRoom = new ListRoom(listData, context);
        binding.listGroup.setHasFixedSize(true);
        binding.listGroup.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.listGroup.setAdapter(listRoom);
        addEventListenRoom();
    }

    private void addEventListenRoom() {
        reference.child("Users").child(phone).child("rooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listData.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Room room = dataSnapshot.getValue(Room.class);
                    assert room != null;
                    if(room.getRoomType().equals("group")) {
                        listData.add(room);
                    }
                }
                binding.loadFragment.setVisibility(View.GONE);
                if(listData.isEmpty()) {
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
}