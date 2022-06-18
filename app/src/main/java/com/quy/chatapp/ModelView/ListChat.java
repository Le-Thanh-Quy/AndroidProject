package com.quy.chatapp.ModelView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.chatapp.Model.Mess;
import com.quy.chatapp.Model.MyToast;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.Notification.SendNotification;
import com.quy.chatapp.R;
import com.quy.chatapp.View.ChatActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListChat extends RecyclerView.Adapter<ListChat.viewHolder> {

    public List<Mess> listData;
    public Context context;
    public String myPhone;
    public User theirUser;
    public String roomID;
    public boolean isGroup;
    DatabaseReference reference;
    Bitmap bitmapAvatar;
    int stopPosition = 0;

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
        holder.their_mess_image.setVisibility(View.GONE);
        holder.my_mess_image.setVisibility(View.GONE);
        holder.my_mess_content.setVisibility(View.GONE);
        holder.their_mess_content.setVisibility(View.GONE);
        holder.my_mess_video.setVisibility(View.GONE);
        holder.their_mess_video.setVisibility(View.GONE);
        holder.their_avatar.setImageBitmap(bitmapAvatar);

        String time = mess.getTime();


        holder.my_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.my_mess_time.getVisibility() == View.GONE) {
                    getTiemMess(time, holder);
                    holder.my_mess_time.setVisibility(View.VISIBLE);
                    holder.my_mess_time.setScaleY(0);
                    ;
                    holder.my_mess_time.animate()
                            .setDuration(300)
                            .scaleYBy(0)
                            .scaleY(1)
                            .setListener(null);
                } else {
                    holder.my_mess_time.setVisibility(View.GONE);
                }

            }
        });

        holder.their_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.their_mess_time.getVisibility() == View.GONE) {
                    getTiemMess(time, holder);
                    holder.their_mess_time.setVisibility(View.VISIBLE);
                    holder.their_mess_time.setScaleY(0);
                    ;
                    holder.their_mess_time.animate()
                            .setDuration(300)
                            .scaleYBy(0)
                            .scaleY(1)
                            .setListener(null);
                } else {
                    holder.their_mess_time.setVisibility(View.GONE);
                }

            }
        });

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
            } else if (contentNotification[0].equals("2")) {
                if (mess.getUserId().equals(myPhone)) {
                    holder.mess_notification.setText("Bạn đã thay đổi biểu tượng cảm xúc cuộc trò chuyện");
                } else {
                    holder.mess_notification.setText(theirUser.getUserName() + " đã thay đổi biểu tượng cảm xúc cuộc trò chuyện");
                }
            } else {
                if (mess.getUserId().equals(myPhone)) {
                    holder.mess_notification.setText("Bạn đã tạo nhóm");
                } else {
                    holder.mess_notification.setText(theirUser.getUserName() + " đã tạo nhóm này");
                }
            }

            return;
        }
        // icon
        if (mess.getType().equals("icon")) {
            int idIcon = context.getResources().getIdentifier("like_mess_" + mess.getMessage(), "drawable", context.getPackageName());
            if (mess.getUserId().equals(myPhone)) {
                holder.my_mess.setVisibility(View.VISIBLE);
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
                holder.their_mess_icon.setVisibility(View.VISIBLE);
                if (mess.getMessage().equals("0")) {
                    holder.their_mess_icon.setColorFilter(Color.parseColor("#192497"));
                }
                holder.their_mess_icon.setImageResource(idIcon);
            }

            holder.mess_icon.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    deleteMess(mess);
                    return false;
                }
            });

            return;
        }

        // image

        if (mess.getType().equals("image")) {

            if (mess.getUserId().equals(myPhone)) {
                holder.my_mess.setVisibility(View.VISIBLE);
                holder.my_mess_image.setVisibility(View.VISIBLE);
                Picasso.get().load(mess.getMessage()).fit().centerCrop().into(holder.my_mess_image);
            } else {
                if ((position + 1) < listData.size()) {
                    if (!listData.get(position + 1).getUserId().equals(myPhone)) {
                        holder.layout_avatar.setVisibility(View.INVISIBLE);
                    } else {
                        holder.layout_avatar.setVisibility(View.VISIBLE);
                    }
                }
                holder.their_mess.setVisibility(View.VISIBLE);
                holder.their_mess_image.setVisibility(View.VISIBLE);
                Picasso.get().load(mess.getMessage()).fit().centerCrop().into(holder.their_mess_image);
            }
            holder.my_mess_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showImage(holder.my_mess_image);
                }
            });

            holder.their_mess_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showImage(holder.their_mess_image);
                }
            });

            holder.my_mess_image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    deleteMess(mess);
                    return false;
                }
            });
            return;
        }

        // video

        if (mess.getType().equals("video")) {

            if (mess.getUserId().equals(myPhone)) {
                holder.my_mess.setVisibility(View.VISIBLE);
                holder.my_mess_video.setVisibility(View.VISIBLE);

            } else {
                if ((position + 1) < listData.size()) {
                    if (!listData.get(position + 1).getUserId().equals(myPhone)) {
                        holder.layout_avatar.setVisibility(View.INVISIBLE);
                    } else {
                        holder.layout_avatar.setVisibility(View.VISIBLE);
                    }
                }
                holder.their_mess.setVisibility(View.VISIBLE);
                holder.their_mess_video.setVisibility(View.VISIBLE);
            }
            holder.my_mess_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showVideo(mess.getMessage());
                }
            });

            holder.their_mess_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showVideo(mess.getMessage());
                }
            });

            holder.my_mess_video.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    deleteMess(mess);
                    return false;
                }
            });
            return;
        }

        // deelete
        if (mess.getType().equals("delete")) {
            if (mess.getUserId().equals(myPhone)) {
                holder.my_mess.setVisibility(View.VISIBLE);
                holder.my_mess_content.setVisibility(View.VISIBLE);
                holder.my_mess_content.setText("Tin nhắn đã bị thu hồi");
                holder.my_mess_content.setTextColor(Color.parseColor("#FFCDCDCD"));
                holder.my_mess_content.setBackgroundResource(R.drawable.border_mess_remove);
            } else {
                if ((position + 1) < listData.size()) {
                    if (!listData.get(position + 1).getUserId().equals(myPhone)) {
                        holder.layout_avatar.setVisibility(View.INVISIBLE);
                    } else {
                        holder.layout_avatar.setVisibility(View.VISIBLE);
                    }
                }
                holder.their_mess.setVisibility(View.VISIBLE);
                holder.their_mess_content.setVisibility(View.VISIBLE);
                holder.their_mess_content.setText("Tin nhắn đã bị thu hồi");
                holder.their_mess_content.setTextColor(Color.parseColor("#FFCDCDCD"));
                holder.their_mess_content.setBackgroundResource(R.drawable.border_mess_remove);
            }
            return;
        }

        // call
        if (mess.getType().equals("call")) {
            boolean isCall = false;
            if (mess.getMessage().contains(":")) {
                isCall = true;
            }
            Drawable img = context.getResources().getDrawable(R.drawable.ic_call);
            if (isCall) {
                img.setTint(Color.BLUE);
            } else {
                img.setTint(Color.RED);
            }
            if (mess.getUserId().equals(myPhone)) {
                holder.my_mess.setVisibility(View.VISIBLE);
                holder.my_mess_content.setVisibility(View.VISIBLE);
                holder.my_mess_content.setText(mess.getMessage());
                holder.my_mess_content.setTextColor(Color.BLACK);
                holder.my_mess_content.setBackgroundResource(R.drawable.border_their_mess);
                holder.my_mess_content.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            } else {
                if ((position + 1) < listData.size()) {
                    if (!listData.get(position + 1).getUserId().equals(myPhone)) {
                        holder.layout_avatar.setVisibility(View.INVISIBLE);
                    } else {
                        holder.layout_avatar.setVisibility(View.VISIBLE);
                    }
                }
                holder.their_mess.setVisibility(View.VISIBLE);
                holder.their_mess_content.setVisibility(View.VISIBLE);
                holder.their_mess_content.setText(mess.getMessage());
                holder.their_mess_content.setTextColor(Color.BLACK);
                holder.their_mess_content.setBackgroundResource(R.drawable.border_their_mess);
                holder.their_mess_content.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            }
            return;
        }

        if (myPhone.equals(mess.getUserId())) {
            holder.my_mess.setVisibility(View.VISIBLE);
            holder.my_mess_content.setVisibility(View.VISIBLE);
            holder.my_mess_content.setText(mess.getMessage());
            holder.my_mess_content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    deleteMess(mess);
                    return false;
                }
            });

            // UI

            if (listData.size() > 1) {
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
            }
            // END

        } else {
            holder.their_mess.setVisibility(View.VISIBLE);
            holder.their_mess_content.setVisibility(View.VISIBLE);
            holder.their_mess_content.setText(mess.getMessage());

            if ((position + 1) < listData.size()) {
                if (!listData.get(position + 1).getUserId().equals(myPhone)) {
                    holder.layout_avatar.setVisibility(View.INVISIBLE);
                } else {
                    holder.layout_avatar.setVisibility(View.VISIBLE);
                }
            }

            // UI

            if (listData.size() > 1) {
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
            }

            // END


        }
    }

    private void getTiemMess(String time, viewHolder holder) {
        long lassMessTime = Long.parseLong(time);
        long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lassMessTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lassMessTime);
        if (minutes == 0) {
            holder.my_mess_time.setText("Vừa xong");
            holder.their_mess_time.setText("Vừa xong");
        } else if (minutes < 60) {
            holder.my_mess_time.setText(minutes + " phút trước");
            holder.their_mess_time.setText(minutes + " phút trước");
        } else if (hours < 24) {
            holder.my_mess_time.setText(hours + " giờ trước");
            holder.their_mess_time.setText(hours + " giờ trước");
        } else if (hours < 24 * 5 + 1) {
            holder.my_mess_time.setText(hours / 24 + " ngày trước");
            holder.their_mess_time.setText(hours / 24 + " ngày trước");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm");
            Date resultDate = new Date(lassMessTime);
            holder.my_mess_time.setText(sdf.format(resultDate));
            holder.their_mess_time.setText(sdf.format(resultDate));
        }
    }

    private void showImage(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Dialog dialog_image = new Dialog(context, R.style.Dialogs);
        dialog_image.setContentView(R.layout.view_imge);
        ZoomableImageView mess_image = dialog_image.findViewById(R.id.mess_image);
        ImageView back_to_mess = dialog_image.findViewById(R.id.back_to_mess);

        mess_image.setImageBitmap(bitmap);
        back_to_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_image.dismiss();
            }
        });
        dialog_image.show();
    }

    private void showVideo(String videoUrl) {
        Dialog dialog_image = new Dialog(context, R.style.Dialogs);
        dialog_image.setContentView(R.layout.view_video);
        VideoView mess_video = dialog_image.findViewById(R.id.mess_video);
        ImageView back_to_mess = dialog_image.findViewById(R.id.back_to_mess);
        ProgressBar progressBar = dialog_image.findViewById(R.id.progressBar);

        MediaController mc = new MediaController(context) {
            @Override
            public void hide() {
                this.show();
            }
        };
        mess_video.setMediaController(mc);
        mc.setAnchorView(mess_video);
        ((ViewGroup) mc.getParent()).removeView(mc);
        ((FrameLayout) dialog_image.findViewById(R.id.videoViewWrapper)).addView(mc);
        mc.setVisibility(View.VISIBLE);
        mess_video.setVideoPath(videoUrl);
        mess_video.seekTo(0);
        mess_video.start();
        mess_video.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {

                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    progressBar.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });
        back_to_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mc.hide();
                dialog_image.dismiss();
            }
        });
        dialog_image.show();
    }

    private void deleteMess(Mess mess) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        reference.child("Rooms").child(roomID).child("listMess").child(mess.getTime()).child("type").setValue("delete");
                        SendNotification.send(context, theirUser.getToken(), User.getInstance().getUserName(), "Đã thu hồi một tin nhắn", myPhone, "delete", User.getInstance().getUserAvatar(), false);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
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
        public ImageView their_avatar,
                their_seen_their,
                their_seen_me,
                mess_icon,
                their_mess_icon,
                their_mess_image,
                my_mess_image;
        public TextView their_name, their_mess_content, my_mess_content, mess_notification, my_mess_time, their_mess_time;
        public CardView layout_avatar, layout_seen_their, layout_seen;
        public ImageView my_mess_video, their_mess_video;

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
            their_mess_image = view.findViewById(R.id.their_mess_image);
            my_mess_image = view.findViewById(R.id.my_mess_image);
            my_mess_video = view.findViewById(R.id.my_mess_video);
            their_mess_video = view.findViewById(R.id.their_mess_video);
            my_mess_time = view.findViewById(R.id.my_mess_time);
            their_mess_time = view.findViewById(R.id.their_mess_time);
        }
    }
}