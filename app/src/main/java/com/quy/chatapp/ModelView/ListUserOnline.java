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

import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListUserOnline extends RecyclerView.Adapter<ListUserOnline.viewHolder> {

    public List<User> listData;
    public Context context;

    public ListUserOnline(List<User> listData, Context context) {
        this.listData = listData;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_online_item, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = listData.get(position);

        holder.userName.setText(user.getUserName());
        if (!"null".equals(user.getUserAvatar())) {
            Picasso.get().load(user.getUserAvatar()).into(holder.userAvatar);
        }

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        public ImageView userAvatar;
        public CardView userStatus;
        public TextView userName;

        public viewHolder(View view) {
            super(view);
            userAvatar = view.findViewById(R.id.userAvatar);
            userStatus = view.findViewById(R.id.userStatus);
            userName = view.findViewById(R.id.userName);
        }
    }
}
