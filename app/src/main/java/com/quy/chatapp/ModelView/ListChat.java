package com.quy.chatapp.ModelView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;

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
        holder.mess_notification.setVisibility(View.GONE);
        holder.mess_icon.setVisibility(View.GONE);
        holder.their_mess_icon.setVisibility(View.GONE);


        holder.their_avatar.setImageBitmap(bitmapAvatar);

        if (!isGroup) {
            holder.their_name.setVisibility(View.GONE);
        }

        reference.child("Rooms").child(roomID).child("listSeen").child(theirUser.getPhoneNumber()).child("in").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String messId = snapshot.getValue(String.class);
                    holder.layout_seen.setVisibility(View.INVISIBLE);
                    holder.layout_seen_their.setVisibility(View.INVISIBLE);
                    holder.their_seen_me.setVisibility(View.INVISIBLE);
                    holder.their_seen_their.setVisibility(View.INVISIBLE);
                    if (mess.getTime().equals(messId)) {
                        if (myPhone.equals(mess.getUserId())) {
                            holder.layout_seen.setVisibility(View.VISIBLE);
                            holder.their_seen_me.setVisibility(View.VISIBLE);
                            holder.their_seen_me.setImageBitmap(bitmapAvatar);

                        } else {
                            holder.layout_seen_their.setVisibility(View.VISIBLE);
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

        // notification
        if (mess.getType().equals("notification")) {
            holder.mess_notification.setVisibility(View.VISIBLE);
            String[] contentNotification = mess.getMessage().split("__@__");
            if (contentNotification[0].equals("1")) {
                if (mess.getUserId().equals(myPhone)) {
                    holder.mess_notification.setText("Bạn đã đặt biệt hiệu cho " + theirUser.getUserName() + " là " + contentNotification[1]);
                } else {
                    holder.mess_notification.setText(theirUser.getUserName() + " đã đặt biệt hiệu cho bạn là " + contentNotification[1]);
                }

            } else {

            }

            return;
        }
        // icon
        if (mess.getType().equals("icon")) {
            int idIcon = context.getResources().getIdentifier("like_mess_" + mess.getMessage(), "drawable", context.getPackageName());
            if (mess.getUserId().equals(myPhone)) {
                holder.my_mess.setVisibility(View.VISIBLE);
                holder.my_mess_content.setVisibility(View.GONE);
                holder.mess_icon.setVisibility(View.VISIBLE);
                if (mess.getMessage().equals("0")) {
                    holder.mess_icon.setColorFilter(Color.parseColor("#192497"));
                }
                holder.mess_icon.setImageResource(idIcon);
            } else {
                if ((position + 1) < listData.size()) {
                    if (!listData.get(position + 1).getUserId().equals(myPhone)) {
                        holder.layout_avatar.setVisibility(View.INVISIBLE);
                    } else {
                        holder.layout_avatar.setVisibility(View.VISIBLE);
                    }
                }
                holder.their_mess.setVisibility(View.VISIBLE);
                holder.their_mess_content.setVisibility(View.GONE);
                holder.their_mess_icon.setVisibility(View.VISIBLE);
                if (mess.getMessage().equals("0")) {
                    holder.their_mess_icon.setColorFilter(Color.parseColor("#192497"));
                }
                holder.their_mess_icon.setImageResource(idIcon);
            }
            return;
        }


        if (myPhone.equals(mess.getUserId())) {
            holder.my_mess.setVisibility(View.VISIBLE);
            holder.my_mess_content.setText(mess.getMessage());

            // UI

            if (position == 0) {
                if (listData.get(0).getUserId().equals(myPhone) && listData.get(0).getType().equals("text")) {
                    if (listData.get(1).getUserId().equals(myPhone) && listData.get(1).getType().equals("text")) {
                        holder.my_mess_content.setBackgroundResource(R.drawable.border_my_mess_top);
                    } else {
                        holder.my_mess_content.setBackgroundResource(R.drawable.border_my_mess);
                    }

                }
            }

            if (position - 1 >= 0) {
                if (listData.get(position - 1).getUserId().equals(myPhone) && listData.get(position - 1).getType().equals("text")) {
                    if (position + 1 < listData.size()) {
                        if (listData.get(position + 1).getUserId().equals(myPhone) && listData.get(position + 1).getType().equals("text")) {
                            holder.my_mess_content.setBackgroundResource(R.drawable.border_my_mess_center);
                        } else {
                            holder.my_mess_content.setBackgroundResource(R.drawable.border_my_mess_end);
                        }
                    }

                } else {
                    if (position + 1 < listData.size()) {
                        if (listData.get(position + 1).getUserId().equals(myPhone) && listData.get(position + 1).getType().equals("text")) {
                            holder.my_mess_content.setBackgroundResource(R.drawable.border_my_mess_top);
                        } else {
                            holder.my_mess_content.setBackgroundResource(R.drawable.border_my_mess);
                        }
                    }
                }
            }

            if (position == listData.size() - 1) {
                if (listData.get(listData.size() - 1).getUserId().equals(myPhone) && listData.get(listData.size() - 1).getType().equals("text")) {
                    if (listData.get(listData.size() - 2).getUserId().equals(myPhone) && listData.get(listData.size() - 2).getType().equals("text")) {
                        holder.my_mess_content.setBackgroundResource(R.drawable.border_my_mess_end);
                    } else {
                        holder.my_mess_content.setBackgroundResource(R.drawable.border_my_mess);
                    }

                }
            }


            // END

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

            // UI

            if (position == 0) {
                if (listData.get(0).getUserId().equals(theirUser.getPhoneNumber()) && listData.get(0).getType().equals("text")) {
                    if (listData.get(1).getUserId().equals(theirUser.getPhoneNumber()) && listData.get(1).getType().equals("text")) {
                        holder.their_mess_content.setBackgroundResource(R.drawable.border_their_mess_top);
                    } else {
                        holder.their_mess_content.setBackgroundResource(R.drawable.border_their_mess);
                    }

                }
            }
            if (position - 1 >= 0) {
                if (listData.get(position - 1).getUserId().equals(theirUser.getPhoneNumber()) && listData.get(position - 1).getType().equals("text")) {
                    if (position + 1 < listData.size()) {
                        if (listData.get(position + 1).getUserId().equals(theirUser.getPhoneNumber()) && listData.get(position + 1).getType().equals("text")) {
                            holder.their_mess_content.setBackgroundResource(R.drawable.border_their_mess_center);
                        } else {
                            holder.their_mess_content.setBackgroundResource(R.drawable.border_their_mess_bottom);
                        }
                    }

                } else {
                    if (position + 1 < listData.size()) {
                        if (listData.get(position + 1).getUserId().equals(theirUser.getPhoneNumber()) && listData.get(position + 1).getType().equals("text")) {
                            holder.their_mess_content.setBackgroundResource(R.drawable.border_their_mess_top);
                        } else {
                            holder.their_mess_content.setBackgroundResource(R.drawable.border_their_mess);
                        }
                    }
                }
            }

            if (position == listData.size() - 1) {
                if (listData.get(listData.size() - 1).getUserId().equals(theirUser.getPhoneNumber()) && listData.get(listData.size() - 1).getType().equals("text")) {
                    if (listData.get(listData.size() - 2).getUserId().equals(theirUser.getPhoneNumber()) && listData.get(listData.size() - 2).getType().equals("text")) {
                        holder.their_mess_content.setBackgroundResource(R.drawable.border_their_mess_bottom);
                    } else {
                        holder.their_mess_content.setBackgroundResource(R.drawable.border_their_mess);
                    }

                }
            }

            // END



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
        public ImageView their_avatar, their_seen_their, their_seen_me, mess_icon, their_mess_icon;
        public TextView their_name, their_mess_content, my_mess_content, mess_notification;
        public CardView layout_avatar, layout_seen_their, layout_seen;


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
            layout_seen_their = view.findViewById(R.id.layout_seen_their);
            layout_seen = view.findViewById(R.id.layout_seen);
            mess_notification = view.findViewById(R.id.mess_notification);
            mess_icon = view.findViewById(R.id.mess_icon);
            their_mess_icon = view.findViewById(R.id.their_mess_icon);
        }
    }
}