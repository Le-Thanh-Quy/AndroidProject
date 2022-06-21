package com.quy.chatapp.ModelView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;
import com.quy.chatapp.View.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListUserOnline extends RecyclerView.Adapter<ListUserOnline.viewHolder> {

    public List<User> listData;
    public Context context;
    private DatabaseReference reference;

    public ListUserOnline(List<User> listData, Context context) {
        this.listData = listData;
        this.context = context;
        reference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_online_item, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        int currentPosition = position;
        User user = listData.get(position);

        if(user.getUserName().equals("Tạo tin")) {
            holder.userName.setText(user.getUserName());
            holder.userAvatar.setImageDrawable(context.getResources().getDrawable(R.drawable.add_satus));
            holder.userStatus.setVisibility(View.GONE);
            return;
        }
        reference.child("Users").child(user.getPhoneNumber()).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (!snapshot.child("is").getValue(Boolean.class)) {
                        holder.itemView.setVisibility(View.GONE);
                        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                        params.height = 0;
                        params.width = 0;
                        holder.itemView.setLayoutParams(params);
                    } else {
                        holder.itemView.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        params.width = (int) pxFromDp(context, 90f);
                        holder.itemView.setLayoutParams(params);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.userName.setText(user.getUserName());
        if (!"null".equals(user.getUserAvatar())) {
            Picasso.get()
                    .load(user.getUserAvatar()) // web image url
                    .fit().centerInside()
//                    .rotate(90)                    //if you want to rotate by 90 degrees
                    .error(R.drawable.profile)
                    .placeholder(R.drawable.profile)
                    .into(holder.userAvatar);
        } else {
            holder.userAvatar.setImageDrawable(context.getDrawable(R.drawable.profile));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("other_user", user);
                context.startActivity(intent);
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

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
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
