package com.quy.chatapp.ModelView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.quy.chatapp.View.ChatGroupActivity;
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
        final User[] other_user = {new User()};


        try {
            if (room.getRoomType().equals("group")) {
                reference.child("Rooms").child(room.getRoomID()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        String name = task.getResult().child("roomName").getValue(String.class);
                        String image = task.getResult().child("imageRoom").getValue(String.class);
                        holder.roomName.setText(name);
                        try {
                            Glide.with(context)
                                    .load(image)
                                    .centerCrop()
                                    .placeholder(ContextCompat.getDrawable(context, R.drawable.team))
                                    .error(ContextCompat.getDrawable(context, R.drawable.team))
                                    .into(holder.roomImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                holder.roomName.setText(room.getRoomName());
            }
            if (user.getPhoneNumber().equals(room.getLastMessId())) {
                holder.roomLastMess.setText("B???n: " + room.getLastMess());
            } else {
                holder.roomLastMess.setText(room.getLastMess());
            }

            String time = room.getRoomTimeLastMess();
            long lassMessTime = Long.parseLong(time);
            long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lassMessTime);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lassMessTime);
            if (minutes == 0) {
                holder.roomTimeLastMess.setText("V???a xong");
            } else if (minutes < 60) {
                holder.roomTimeLastMess.setText(minutes + " ph??t tr?????c");
            } else if (hours < 24) {
                holder.roomTimeLastMess.setText(hours + " gi??? tr?????c");
            } else if (hours < 24 * 5 + 1) {
                holder.roomTimeLastMess.setText(hours / 24 + " ng??y tr?????c");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm");
                Date resultDate = new Date(lassMessTime);
                holder.roomTimeLastMess.setText(sdf.format(resultDate));
            }

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


            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setBackgroundColor(Color.parseColor("#f0f0f0"));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        v.setBackgroundColor(Color.TRANSPARENT);
                    }
                    return false;
                }
            });

            if (room.getRoomType().equals("group")) {
                holder.roomSeenStatus.setVisibility(View.INVISIBLE);
                holder.roomStatus.setVisibility(View.INVISIBLE);
                holder.roomSeenImage.setVisibility(View.INVISIBLE);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ChatGroupActivity.class);
                        intent.putExtra("id_room", room.getRoomID());
                        context.startActivity(intent);
                    }
                });

            } else {
                reference.child("Users").child(room.getUserId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.getResult().getValue() != null) {
                            other_user[0] = task.getResult().getValue(User.class);
                            try {
                                Glide.with(context).load(other_user[0].getUserAvatar()).centerCrop().placeholder(ContextCompat.getDrawable(context, R.drawable.profile)).into(holder.roomSeenImage);
                                Glide.with(context).load(other_user[0].getUserAvatar()).centerCrop().placeholder(ContextCompat.getDrawable(context, R.drawable.profile)).into(holder.roomImage);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            eventStatus(holder, other_user[0]);
                        }
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
                        if (other_user[0] != null) {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("other_user", other_user[0]);
                            context.startActivity(intent);
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private void eventStatus(@NonNull viewHolder holder, User other_user) {
        reference.child("Users").child(other_user.getPhoneNumber()).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Boolean isOnline = snapshot.child("is").getValue(Boolean.class);
                    if (!isOnline) {
                        holder.roomStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#979797")));
                        String time = snapshot.child("in").getValue(String.class);
                        ;
                        long lassMessTime = Long.parseLong(time);
                        long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lassMessTime);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lassMessTime);
                        holder.timeStatus.setVisibility(View.VISIBLE);
                        if (minutes == 0) {
                            holder.time_status_tv.setText("V???a m???i");
                        } else if (minutes < 60) {
                            holder.time_status_tv.setText(minutes + " ph??t");
                        } else if (hours < 24) {
                            holder.time_status_tv.setText(hours + " gi???");
                        } else {
                            holder.timeStatus.setVisibility(View.GONE);
                        }
                    } else {
                        holder.timeStatus.setVisibility(View.GONE);
                        holder.roomStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5FD364")));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        public ImageView roomImage, roomSeenImage;
        public CardView roomStatus, roomSeenStatus, timeStatus;
        public TextView roomName, time_status_tv;
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
            timeStatus = view.findViewById(R.id.timeStatus);
            time_status_tv = view.findViewById(R.id.time_status_tv);
        }
    }
}
