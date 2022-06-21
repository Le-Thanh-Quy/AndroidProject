package com.quy.chatapp.Fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.chatapp.Model.Mess;
import com.quy.chatapp.Model.MyToast;
import com.quy.chatapp.Model.Room;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.ModelView.ListFriend;
import com.quy.chatapp.ModelView.ListFriendGroup;
import com.quy.chatapp.ModelView.ListRoom;
import com.quy.chatapp.Notification.SendNotification;
import com.quy.chatapp.R;
import com.quy.chatapp.View.ChatActivity;
import com.quy.chatapp.databinding.FragmentFriendBinding;
import com.quy.chatapp.databinding.FragmentGroupBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupFragment extends Fragment {

    private static DatabaseReference reference;
    private static Context context;
    private FragmentGroupBinding binding;
    private static String phone;

    public GroupFragment() {
    }

    public static GroupFragment getInstance(Context context1) {
        GroupFragment instance = new GroupFragment();
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
        binding = FragmentGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listFriendController();
        binding.addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroup();
            }
        });
    }

    Dialog dialog;
    RecyclerView list_friend;
    EditText name_chat;
    TextView create_chat;
    ImageView cancel, not_found;
    ArrayList<User> friends;
    ListFriendGroup listFriendGroup;
    ProgressBar load_fragment;
    boolean isNameExactly = false;
    public static boolean isEnough = false;

    public void createGroup() {
        dialog = new Dialog(context, R.style.Dialogs);
        dialog.setContentView(R.layout.create_group);
        cancel = dialog.findViewById(R.id.cancel);
        create_chat = dialog.findViewById(R.id.create_chat);
        name_chat = dialog.findViewById(R.id.name_chat);
        list_friend = dialog.findViewById(R.id.list_friend);
        not_found = dialog.findViewById(R.id.not_found);
        load_fragment = dialog.findViewById(R.id.load_fragment);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        name_chat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() >= 2) {
                    isNameExactly = true;
                } else {
                    isNameExactly = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isEnough && isNameExactly) {
                        create_chat.setTextColor(Color.BLACK);
                    } else {
                        create_chat.setTextColor(Color.parseColor("#C8C8C8"));
                    }
                }
            }
        });

        friends = new ArrayList<User>();
        listFriendGroup = new ListFriendGroup(friends, context);
        list_friend.setHasFixedSize(true);
        list_friend.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        list_friend.setAdapter(listFriendGroup);
        addEventListenFriend();

        create_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnough && isNameExactly) {
                    ProgressDialog progress = new ProgressDialog(context);
                    progress.setTitle("Loading");
                    progress.setMessage("Creating...");
                    progress.setCancelable(false);
                    try {
                        progress.show();
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                    String idRoom = String.valueOf(System.currentTimeMillis());
                    String nameGroup = name_chat.getText().toString().trim();
                    Mess mess = new Mess();
                    mess.setMessage("3__@__");
                    mess.setTime(idRoom);
                    mess.setType("notification");
                    mess.setUserId(phone);
                    reference.child("Rooms").child(idRoom).child("listMess").child(idRoom).setValue(mess);
                    reference.child("Rooms").child(idRoom).child("listSeen").child(phone).child("is").setValue(true);
                    Room room = new Room();
                    room.setRoomID(idRoom);
                    room.setRoomType("group");
                    room.setLastMess(User.getInstance().getUserName() + " đã tạo nhóm");
                    room.setLastMessId(phone);
                    room.setRoomTimeLastMess(idRoom);
                    reference.child("Users").child(phone).child("rooms").child(idRoom).setValue(room);
                    for (User user : ListFriendGroup.listUser) {
                        reference.child("Rooms").child(idRoom).child("listSeen").child(user.getPhoneNumber()).child("is").setValue(false);
                        reference.child("Rooms").child(idRoom).child("roomName").setValue(nameGroup);
                        reference.child("Rooms").child(idRoom).child("imageRoom").setValue("null");
                        SendNotification.send(
                                context, user.getToken(),
                                nameGroup,
                                User.getInstance().getUserName() + " đã tạo nhóm",
                                idRoom,
                                "text",
                                User.getInstance().getUserAvatar(),
                                true
                        );
                        reference.child("Users").child(user.getPhoneNumber()).child("rooms").child(idRoom).setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(progress.isShowing()) {
                                    progress.dismiss();
                                }
                                if(dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                }
            }
        });

        dialog.show();
    }

    private void addEventListenFriend() {
        reference.child("Users").child(phone).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friends.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userPhoneNumber = dataSnapshot.getValue(String.class);
                    assert userPhoneNumber != null;
                    reference.child("Users").child(userPhoneNumber).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.getResult().getValue() != null) {
                                User user = task.getResult().getValue(User.class);
                                assert user != null;
                                friends.add(user);
                                if (friends.isEmpty()) {
                                    not_found.setVisibility(View.VISIBLE);
                                } else {
                                    not_found.setVisibility(View.GONE);
                                }
                                listFriendGroup.notifyDataSetChanged();
                            }
                        }
                    });
                }
                load_fragment.setVisibility(View.GONE);
                listFriendGroup.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    List<Room> listData;
    ListRoom listRoom;

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
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Room room = dataSnapshot.getValue(Room.class);
                    assert room != null;
                    if (room.getRoomType().equals("group")) {
                        listData.add(room);
                    }
                }
                binding.loadFragment.setVisibility(View.GONE);
                if (listData.isEmpty()) {
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