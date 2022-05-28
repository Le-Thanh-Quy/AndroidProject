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

import com.quy.chatapp.Model.User;
import com.quy.chatapp.ModelView.ListFriend;
import com.quy.chatapp.R;
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
        listData = Arrays.asList(
                new User("null", "Lê Thanh Quý", "0384933379", true, "123123"),
                new User("null", "Trần Nguyễn Anh Trình","0384933379", true, "123123"),
                new User("null", "Thanh Quý", "0384933379", true, "123123"),
                new User("null", "Lê Quý", "0384933379", true, "123123"),
                new User("null", "Lê Quý Thanh", "0384933379", true, "123123"),
                new User("null", "Thanh Quý Lê", "0384933379", true, "123123"),
                new User("null", "Lê Thanh Quý", "0384933379", true, "123123"),
                new User("null", "Lê Thanh Quý", "0384933379", true, "123123"),
                new User("null", "Lê Thanh Quý", "0384933379", true, "123123")

        );
        listFriend = new ListFriend(listData, context);
        binding.listFriend.setHasFixedSize(true);
        binding.listFriend.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.listFriend.setAdapter(listFriend);
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
                if (charSequence.toString().matches(validNumber)) {
                    result_search.setVisibility(View.VISIBLE);
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
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}