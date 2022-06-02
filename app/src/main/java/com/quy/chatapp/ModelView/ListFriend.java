package com.quy.chatapp.ModelView;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.chatapp.Model.Room;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;
import com.quy.chatapp.View.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListFriend extends RecyclerView.Adapter<ListFriend.viewHolder> {

    public List<User> listData;
    public Context context;
    private DatabaseReference reference;

    public ListFriend(List<User> listData, Context context) {
        this.listData = listData;
        this.context = context;
        reference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ListFriend.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_item, parent, false);
        return new ListFriend.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListFriend.viewHolder holder, int position) {
        User user = listData.get(position);

        reference.child("Users").child(user.getPhoneNumber()).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (!snapshot.getValue(Boolean.class)) {
                        holder.friendStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#979797")));
                    } else {
                        holder.friendStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5FD364")));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.friendName.setText(user.getUserName());
        if (!"null".equals(user.getUserAvatar())) {
            Picasso.get()
                    .load(user.getUserAvatar()) // web image url
                    .fit().centerInside()
//                    .rotate(90)                    //if you want to rotate by 90 degrees
                    .error(R.drawable.profile)
                    .placeholder(R.drawable.profile)
                    .into(holder.friendAvatar);
        } else {
            holder.friendAvatar.setImageDrawable(context.getDrawable(R.drawable.profile));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("other_user", user);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        public ImageView friendAvatar;
        public CardView friendStatus;
        public TextView friendName;

        public viewHolder(View view) {
            super(view);
            friendAvatar = view.findViewById(R.id.friendAvatar);
            friendStatus = view.findViewById(R.id.friendStatus);
            friendName = view.findViewById(R.id.friendName);
        }
    }
}