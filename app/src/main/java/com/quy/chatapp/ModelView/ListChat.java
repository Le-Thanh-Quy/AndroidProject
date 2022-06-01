package com.quy.chatapp.ModelView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.chatapp.Model.Mess;
import com.quy.chatapp.R;
import com.quy.chatapp.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListChat extends RecyclerView.Adapter<ListChat.viewHolder> {

    public List<Mess> listData;
    public Context context;
    public String myPhone;
    public User theirUser;
    public String roomID;
    public boolean isGroup;
    DatabaseReference reference;
    Bitmap bitmapAvatar;

    public ListChat(List<Mess> listData, Context context, String myPhone, boolean isGroup, User theirUser, String roomID, Bitmap bitmapAvatar) {
        this.listData = listData;
        this.context = context;
        this.myPhone = myPhone;
        this.isGroup = isGroup;
        this.roomID = roomID;
        this.theirUser = theirUser;
        this.bitmapAvatar = bitmapAvatar;
        reference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Mess mess = listData.get(position);
        holder.my_mess.setVisibility(View.GONE);
        holder.their_mess.setVisibility(View.GONE);
        if (!isGroup) {
            holder.their_name.setVisibility(View.GONE);
        }

        reference.child("Rooms").child(roomID).child("listSeen").child(theirUser.getPhoneNumber()).child("in").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String messId = snapshot.getValue(String.class);
                    holder.their_seen_me.setVisibility(View.INVISIBLE);
                    holder.their_seen_their.setVisibility(View.INVISIBLE);
                    if (mess.getTime().equals(messId)) {
                        if (myPhone.equals(mess.getUserId())) {
                            holder.their_seen_me.setVisibility(View.VISIBLE);
                            holder.their_seen_me.setImageBitmap(bitmapAvatar);

                        } else {
                            holder.their_seen_their.setVisibility(View.VISIBLE);
                            holder.their_seen_their.setImageBitmap(bitmapAvatar);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        if (myPhone.equals(mess.getUserId())) {
            holder.my_mess.setVisibility(View.VISIBLE);
            holder.my_mess_content.setText(mess.getMessage());

        } else {
            holder.their_mess.setVisibility(View.VISIBLE);
            holder.their_mess_content.setText(mess.getMessage());

            if ((position + 1) < listData.size()) {
                if (!listData.get(position + 1).getUserId().equals(myPhone)) {
                    holder.layout_avatar.setVisibility(View.INVISIBLE);
                } else {
                    holder.layout_avatar.setVisibility(View.VISIBLE);
                }
            }

            holder.their_avatar.setImageBitmap(bitmapAvatar);


        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout their_mess, my_mess;
        public ImageView their_avatar, their_seen_their, their_seen_me;
        public TextView their_name, their_mess_content, my_mess_content;
        public CardView layout_avatar;


        public viewHolder(View view) {
            super(view);
            their_mess = view.findViewById(R.id.their_mess);
            my_mess = view.findViewById(R.id.my_mess);
            their_avatar = view.findViewById(R.id.their_avatar);
            their_seen_their = view.findViewById(R.id.their_seen_their);
            their_seen_me = view.findViewById(R.id.their_seen_me);
            their_name = view.findViewById(R.id.their_name);
            their_mess_content = view.findViewById(R.id.their_mess_content);
            my_mess_content = view.findViewById(R.id.my_mess_content);
            layout_avatar = view.findViewById(R.id.layout_avatar);
        }
    }
}