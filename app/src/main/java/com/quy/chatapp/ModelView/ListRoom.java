package com.quy.chatapp.ModelView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
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

public class ListRoom extends RecyclerView.Adapter<ListRoom.viewHolder> {

    public List<Room> listData;
    public Context context;

    public ListRoom(List<Room> listData, Context context) {
        this.listData = listData;
        this.context = context;
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

//        holder.roomName.setText(room.getRoomName());
//        holder.roomLastMess.setText(room.getLastMess());
//        String time = room.getRoomTimeLastMess();
//        String[] arrTime = time.split(" ");
//        holder.roomTimeLastMess.setText("- " + arrTime[1] + " " + arrTime[2]);
//        if(room.getRoomType().equals("group")) {
//            holder.roomSeenStatus.setVisibility(View.INVISIBLE);
//            holder.roomStatus.setVisibility(View.INVISIBLE);
//        }
//        if (!"null".equals(room.getImageRoom())) {
//            Picasso.get().load(room.getImageRoom()).into(holder.roomImage);
//        }
//
//        if(!room.isSeen()) {
//            holder.roomLastMess.setTypeface(null, Typeface.BOLD);
//            holder.roomName.setTypeface(null, Typeface.BOLD);
//            holder.roomTimeLastMess.setTypeface(null, Typeface.BOLD);
//        }
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
