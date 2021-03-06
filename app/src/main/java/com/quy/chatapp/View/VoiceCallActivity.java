package com.quy.chatapp.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quy.chatapp.Model.MyToast;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;
import com.quy.chatapp.databinding.ActivityVoiceCallBinding;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VoiceCallActivity extends AppCompatActivity {

    ActivityVoiceCallBinding binding;
    DatabaseReference reference;
    User user;
    User theirUser;
    boolean isCall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVoiceCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        reference = FirebaseDatabase.getInstance().getReference();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            theirUser = (User) bundle.getSerializable("theirUser");
            user = (User) bundle.getSerializable("user");
        }

        addVoiceCallApi();

    }

    @Override
    protected void onPause() {
        reference.child("Users").child(theirUser.getPhoneNumber()).child("isCall").setValue(null);
        reference.child("Users").child(user.getPhoneNumber()).child("isCall").setValue(null);
        if (call != null) {
            call.hangup();
        }
        if (isCall) {
            setResult(Activity.RESULT_OK, getIntent().putExtra("status", "Cu???c g???i tho???i\n" + binding.time.getText()));
        } else {
            setResult(Activity.RESULT_OK, getIntent().putExtra("status", "Cu???c g???i nh???"));
        }
        finishAndRemoveTask();
        MainActivity.id_user = "";
        reference.child("Users").child(user.getPhoneNumber()).child("status").child("is").setValue(false);
        reference.child("Users").child(user.getPhoneNumber()).child("status").child("in").setValue(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }

    @Override
    protected void onResume() {
        MainActivity.id_user = theirUser.phoneNumber;
        reference.child("Users").child(user.getPhoneNumber()).child("status").child("is").setValue(true);
        super.onResume();
    }

    private Call call;
    SinchClient sinchClient;
    private final String APIKEY = "c2fd6057-7941-4f77-bf39-7381fbf0d113";
    private final String SECRET = "OSLunM3Yr0S0nBoHybcrGQ==";

    private void addVoiceCallApi() {
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this.getApplicationContext())
                .applicationKey(APIKEY)
                .applicationSecret(SECRET)
                .environmentHost("clientapi.sinch.com")
                .userId(user.getPhoneNumber())
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        binding.name.setText(theirUser.getUserName());
        try {
            Glide.with(VoiceCallActivity.this).load(theirUser.getUserAvatar()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    Call();
                    binding.hangup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            countDownTimer.cancel();
                            if (isCall) {
                                setResult(Activity.RESULT_OK, getIntent().putExtra("status", "Cu???c g???i tho???i\n" + binding.time.getText()));
                            } else {
                                setResult(Activity.RESULT_OK, getIntent().putExtra("status", "Cu???c g???i nh???"));
                            }
                            reference.child("Users").child(theirUser.getPhoneNumber()).child("isCall").setValue(null);
                            reference.child("Users").child(user.getPhoneNumber()).child("isCall").setValue(null);
                            call.hangup();
                            finishAndRemoveTask();
                        }
                    });
                    return false;
                }
            }).into(binding.avatar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        binding.status.setText("??ang g???i...");
    }

    CountDownTimer countDownTimer;

    void Call() {
        try {
            call = sinchClient.getCallClient().callUser(theirUser.getPhoneNumber());
            call.addCallListener(new VoiceCallActivity.SinchCallListener());
            countDownTimer = new CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    if (call != null) {
                        call.hangup();
                        if (isCall) {
                            setResult(Activity.RESULT_OK, getIntent().putExtra("status", "Cu???c g???i tho???i\n" + binding.time.getText()));
                        } else {
                            setResult(Activity.RESULT_OK, getIntent().putExtra("status", "Cu???c g???i nh???"));
                        }
                        reference.child("Users").child(theirUser.getPhoneNumber()).child("isCall").setValue(null);
                        reference.child("Users").child(user.getPhoneNumber()).child("isCall").setValue(null);
                        MyToast.show(VoiceCallActivity.this, theirUser.getUserName() + " kh??ng tr??? l???i", 0);
                        finishAndRemoveTask();
                    }
                }

            };
            countDownTimer.start();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Call();
        }
    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            countDownTimer.cancel();
            if (isCall) {
                setResult(Activity.RESULT_OK, getIntent().putExtra("status", "Cu???c g???i tho???i\n" + binding.time.getText()));
            } else {
                setResult(Activity.RESULT_OK, getIntent().putExtra("status", "Cu???c g???i nh???"));
            }
            reference.child("Users").child(theirUser.getPhoneNumber()).child("isCall").setValue(null);
            reference.child("Users").child(user.getPhoneNumber()).child("isCall").setValue(null);
            finishAndRemoveTask();
            call = null;
            SinchError a = endedCall.getDetails().getError();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onCallEstablished(Call establishedCall) {
            isCall = true;
            binding.time.setVisibility(View.VISIBLE);
            binding.status.setVisibility(View.GONE);
            binding.time.setBase(SystemClock.elapsedRealtime());
            binding.time.start();
            countDownTimer.cancel();
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
        }
    }

}