package com.quy.chatapp.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.ModelView.ListFriend;
import com.quy.chatapp.R;
import com.quy.chatapp.databinding.FragmentFriendBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendFragment extends Fragment {

    private DatabaseReference reference;
    Context context;
    FragmentFriendBinding binding;
    String phone;

    public FriendFragment(Context context) {
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
        binding = FragmentFriendBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listFriendController();
        addEvent();
    }

    List<User> listData;
    ListFriend listFriend;

    private void listFriendController() {
        listData = new ArrayList<User>();
        listFriend = new ListFriend(listData, context);
        binding.listFriend.setHasFixedSize(true);
        binding.listFriend.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.listFriend.setAdapter(listFriend);
        addEventListenFriend();
    }

    private void addEventListenFriend() {
        reference.child("Users").child(phone).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listData.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userPhoneNumber = dataSnapshot.getValue(String.class);
                    assert userPhoneNumber != null;
                    reference.child("Users").child(userPhoneNumber).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.getResult().getValue() != null) {
                                User user = task.getResult().getValue(User.class);
                                assert user != null;
                                listData.add(user);
                                if(listData.isEmpty()) {
                                    binding.notFound.setVisibility(View.VISIBLE);
                                } else {
                                    binding.notFound.setVisibility(View.GONE);
                                }
                                listFriend.notifyDataSetChanged();
                            }
                        }
                    });
                }
                binding.loadFragment.setVisibility(View.GONE);
                listFriend.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void addEvent() {
        binding.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogAddFriend();
            }
        });
    }

    private void openDialogAddFriend() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog_add_friend);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView avatar_result = dialog.findViewById(R.id.avatar_result);
        EditText phone_number = dialog.findViewById(R.id.phone_number);
        TextView name_result = dialog.findViewById(R.id.name_result);
        CardView yes_add_friend = dialog.findViewById(R.id.yes_add_friend);
        CardView no_add_friend = dialog.findViewById(R.id.no_add_friend);
        RelativeLayout result_search = dialog.findViewById(R.id.result_search);

        phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String validNumber = "^[+|0]{1}[0-9]{9,11}$";
                String search_phone = charSequence.toString().trim();
                if (search_phone.length() > 0) {
                    if (search_phone.charAt(0) != '+') {
                        search_phone = "+84" + search_phone.substring(1);
                    }
                }
                if (search_phone.matches(validNumber)) {
                    reference.child("Users").child(search_phone).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.getResult().exists()) {
                                User user = task.getResult().getValue(User.class);
                                name_result.setText(user.getUserName());
                                if (!"null".equals(user.getUserAvatar())) {
                                    Picasso.get()
                                            .load(user.getUserAvatar())
                                            .fit().centerInside()
//                                            .rotate(90)
                                            .error(R.drawable.profile)
                                            .placeholder(R.drawable.profile)
                                            .into(avatar_result);
                                } else {
                                    avatar_result.setImageDrawable(context.getDrawable(R.drawable.profile));
                                }
                                result_search.setVisibility(View.VISIBLE);
                            } else {
                                result_search.setVisibility(View.GONE);
                            }
                        }
                    });

                } else {
                    result_search.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        no_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String search_phone = phone_number.getText().toString().trim();
                if (search_phone.length() > 0) {
                    if (search_phone.charAt(0) != '+') {
                        search_phone = "+84" + search_phone.substring(1);
                    }
                }
                reference.child("Users").child(phone).child("friends").child(search_phone).setValue(null);
                dialog.dismiss();
            }
        });
        yes_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String search_phone = phone_number.getText().toString().trim();
                if (search_phone.length() > 0) {
                    if (search_phone.charAt(0) != '+') {
                        search_phone = "+84" + search_phone.substring(1);
                    }
                }
                if (search_phone.equals(phone)) {
                    Toast.makeText(context, "You can't add yourself", Toast.LENGTH_SHORT).show();
                    return;
                }
                reference.child("Users").child(phone).child("friends").child(search_phone).setValue(search_phone);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}