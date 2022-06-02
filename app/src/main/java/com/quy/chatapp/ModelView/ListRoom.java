package com.quy.chatapp.ModelView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListRoom extends RecyclerView.Adapter<ListRoom.viewHolder> {

    public List<Room> listData;
    public Context context;
    private DatabaseReference reference;
    User user;
    User other_user;

    public ListRoom(List<Room> listData, Context context) {
        this.listData = listData;
        this.context = context;
        reference = FirebaseDatabase.getInstance().getReference();
        user = User.getInstance();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.room_item, parent, false);
        return new viewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Room room = listData.get(position);

        holder.roomName.setText(room.getRoomName());
        holder.roomLastMess.setText(room.getLastMess());
        String time = room.getRoomTimeLastMess();
        long lassMessTime = Long.parseLong(time);
        long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lassMessTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lassMessTime);
        if (minutes == 0) {
            holder.roomTimeLastMess.setText("Vừa xong");
        } else if (minutes < 60) {
            holder.roomTimeLastMess.setText(minutes + " phút trước");
        } else if (hours < 24) {
            holder.roomTimeLastMess.setText(hours + " giờ trước");
        } else if (hours < 24 * 5 + 1) {
            holder.roomTimeLastMess.setText(hours / 24 + " ngày trước");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm");
            Date resultDate = new Date(lassMessTime);
            holder.roomTimeLastMess.setText(sdf.format(resultDate));
        }

        if (room.getRoomType().equals("group")) {
            holder.roomSeenStatus.setVisibility(View.INVISIBLE);
            holder.roomStatus.setVisibility(View.INVISIBLE);
            if (!"null".equals(room.getImageRoom())) {
                Picasso.get().load(room.getImageRoom()).fit().centerCrop().placeholder(context.getResources().getDrawable(R.drawable.profile)).into(holder.roomImage, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        BitmapDrawable drawable = (BitmapDrawable) holder.roomImage.getDrawable();
                        Bitmap bitmapAvatar = drawable.getBitmap();
                        holder.roomSeenImage.setImageBitmap(bitmapAvatar);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

            }

            return;
        }


        reference.child("Users").child(room.getUserId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.getResult().getValue() != null) {
                    other_user = task.getResult().getValue(User.class);
                    Picasso.get().load(other_user.getUserAvatar()).fit().centerCrop().placeholder(context.getResources().getDrawable(R.drawable.profile)).into(holder.roomImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            BitmapDrawable drawable = (BitmapDrawable) holder.roomImage.getDrawable();
                            Bitmap bitmapAvatar = drawable.getBitmap();
                            holder.roomSeenImage.setImageBitmap(bitmapAvatar);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            }
        });


        reference.child("Rooms").child(room.getRoomID()).child("listSeen").child(user.getPhoneNumber()).child("is").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue(Boolean.class)) {
                        holder.roomLastMess.setTypeface(null, Typeface.NORMAL);
                        holder.roomName.setTypeface(null, Typeface.NORMAL);
                        holder.roomTimeLastMess.setTypeface(null, Typeface.NORMAL);
                        holder.roomLastMess.setTextColor(Color.parseColor("#494949"));
                        holder.roomName.setTextColor(Color.parseColor("#494949"));
                    } else {

                        holder.roomLastMess.setTypeface(null, Typeface.BOLD);
                        holder.roomName.setTypeface(null, Typeface.BOLD);
                        holder.roomTimeLastMess.setTypeface(null, Typeface.BOLD);
                        holder.roomLastMess.setTextColor(Color.BLACK);
                        holder.roomName.setTextColor(Color.BLACK);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.child("Rooms").child(room.getRoomID()).child("listSeen").child(room.getUserId()).child("is").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue(Boolean.class)) {
                        holder.roomSeenImage.setVisibility(View.VISIBLE);
                    } else {
                        holder.roomSeenImage.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(other_user != null) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("other_user", other_user);
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        public ImageView roomImage, roomSeenImage;
        public CardView roomStatus, roomSeenStatus;
        public TextView roomName;
        public TextView roomLastMess;
        public TextView roomTimeLastMess;

        public viewHolder(View view) {
            super(view);
            roomImage = view.findViewById(R.id.roomImage);
            roomStatus = view.findViewById(R.id.roomStatus);
            roomName = view.findViewById(R.id.roomName);
            roomLastMess = view.findViewById(R.id.roomLastMess);
            roomTimeLastMess = view.findViewById(R.id.roomTimeLastMess);
            roomSeenImage = view.findViewById(R.id.roomSeenImage);
            roomSeenStatus = view.findViewById(R.id.roomSeenStatus);
        }
    }
}
