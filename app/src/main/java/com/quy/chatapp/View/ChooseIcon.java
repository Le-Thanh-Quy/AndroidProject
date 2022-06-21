package com.quy.chatapp.View;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;

public class ChooseIcon extends Activity {
    DatabaseReference reference;
    TableLayout myTableLayout;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_icon);
        reference = FirebaseDatabase.getInstance().getReference();
        user = User.getInstance();
        myTableLayout = findViewById(R.id.table_sticker);
        int soCot = 5;
        int soDong = 5;

        for (int i = 1; i <= soDong; i++) {
            TableRow tableRow = new TableRow(this);
            for (int j = 0; j <= soCot - 1; j++) {
                int ViTri = soCot * (i - 1) + j;
                ImageView imageView = new ImageView(this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 128, 3);
                imageView.setLayoutParams(layoutParams);
                layoutParams.setMargins(20, 20, 20, 20);
                imageView.setPadding(10, 10, 10, 10);
                int idHinh = getResources().getIdentifier("like_mess_" + ViTri, "drawable", getPackageName());
                if(ViTri == 0) {
                    imageView.setColorFilter(Color.parseColor("#192497"));
                }
                imageView.setImageResource(idHinh);
                tableRow.addView(imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("icon", ViTri);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
            myTableLayout.addView(tableRow);
        }
    }


    @Override
    protected void onPause() {
        reference.child("Users").child(user.getPhoneNumber()).child("status").child("is").setValue(false);
        reference.child("Users").child(user.getPhoneNumber()).child("status").child("in").setValue(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }

    @Override
    protected void onResume() {
        reference.child("Users").child(user.getPhoneNumber()).child("status").child("is").setValue(true);
        super.onResume();
    }
}