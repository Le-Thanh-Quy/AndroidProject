package com.quy.chatapp.ModelView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.quy.chatapp.Model.Room;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListFriend extends RecyclerView.Adapter<ListFriend.viewHolder> {

    public List<User> listData;
    public Context context;

    public ListFriend(List<User> listData, Context context) {
        this.listData = listData;
        this.context = context;
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

        holder.friendName.setText(user.getUserName());
        if (!"null".equals(user.getUserAvatar())) {
            Picasso.get().load(user.getUserAvatar()).into(holder.friendAvatar);
        }
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