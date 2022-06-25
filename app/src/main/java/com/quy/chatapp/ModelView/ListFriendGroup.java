package com.quy.chatapp.ModelView;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.chatapp.Fragment.GroupFragment;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;
import com.quy.chatapp.View.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;



public class ListFriendGroup extends RecyclerView.Adapter<ListFriendGroup.viewHolder> {

    public List<User> listData;
    public Context context;
    private DatabaseReference reference;
    public static List<User> listUser;

    public ListFriendGroup(List<User> listData, Context context) {
        this.listData = listData;
        this.context = context;
        reference = FirebaseDatabase.getInstance().getReference();
        listUser = new ArrayList<>();
    }

    @NonNull
    @Override
    public ListFriendGroup.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_add_group_item, parent, false);
        return new ListFriendGroup.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListFriendGroup.viewHolder holder, int position) {
        User user = listData.get(position);
        holder.friendName.setText(user.getUserName());
        if (!"null".equals(user.getUserAvatar())) {
            try {
                Glide.with(context)
                        .load(user.getUserAvatar())
                        .centerInside()
                        .error(R.drawable.profile)
                        .placeholder(R.drawable.profile)
                        .into(holder.friendAvatar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.friendAvatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.profile));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.isChoose.isChecked()) {
                    listUser.remove(user);
                    holder.isChoose.setChecked(false);
                } else {
                    listUser.add(user);
                    holder.isChoose.setChecked(true);
                }
                if(listUser.size() >= 2) {
                    GroupFragment.isEnough = true;
                } else {
                    GroupFragment.isEnough = false;
                }
            }
        });

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    v.setBackgroundColor(Color.parseColor("#f0f0f0"));
                }
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                {
                    v.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        public ImageView friendAvatar;
        public TextView friendName;
        public RadioButton isChoose;

        public viewHolder(View view) {
            super(view);
            friendAvatar = view.findViewById(R.id.friendAvatar);
            friendName = view.findViewById(R.id.friendName);
            isChoose = view.findViewById(R.id.isChoose);
        }
    }
}
