package com.quy.chatapp.Notification;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;
import com.quy.chatapp.View.ChatActivity;
import com.quy.chatapp.View.MainActivity;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

public class PushVoiceCall {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    @SuppressLint("StaticFieldLeak")
    public static Activity activity;
    public static User user;
    public static Call call;
    public static SinchClient sinchClient;
    public static String APIKEY = "c2fd6057-7941-4f77-bf39-7381fbf0d113";
    public static String SECRET = "OSLunM3Yr0S0nBoHybcrGQ==";
    private static Dialog dialog;
    private static CardView hangup;
    private static CardView call_start;
    private static Chronometer time;
    private static TextView status;
    private static TextView name;
    private static ImageView avatar;
    private static Space center_space;
    private static DatabaseReference reference;
    private static String theirUserPhone;
    private static MediaPlayer mediaPlayer;
    private static String tempUserName;

    public static void addVoiceCallApi() {
        reference = FirebaseDatabase.getInstance().getReference();
        sinchClient = Sinch.getSinchClientBuilder()
                .context(context)
                .applicationKey(APIKEY)
                .applicationSecret(SECRET)
                .environmentHost("clientapi.sinch.com")
                .userId(user.getPhoneNumber())
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        CallClient callClient = sinchClient.getCallClient();
        callClient.addCallClientListener(new SinchCallClientListener());
    }

    private static class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            MainActivity.id_user = tempUserName;
            if(mediaPlayer != null) {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            }
            call = null;
            SinchError a = endedCall.getDetails().getError();
            activity.setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            tempUserName = MainActivity.id_user;
            MainActivity.id_user = theirUserPhone;
            if(mediaPlayer != null) {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            }
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
        }
    }


    private static class SinchCallClientListener implements CallClientListener {

        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            call = incomingCall;

            sinchClient.getAudioController().enableSpeaker();
            dialog = new Dialog(context, R.style.Dialogs);
            dialog.setContentView(R.layout.push_voice_call);
            hangup = dialog.findViewById(R.id.hangup);
            call_start = dialog.findViewById(R.id.call);
            time = dialog.findViewById(R.id.time);
            status = dialog.findViewById(R.id.status);
            name = dialog.findViewById(R.id.name);
            avatar = dialog.findViewById(R.id.avatar);
            center_space = dialog.findViewById(R.id.center_space);
            status.setText("Đang gọi");

            mediaPlayer = MediaPlayer.create(context, R.raw.voice_call);
            final AudioManager mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            final int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

            mediaPlayer.setLooping(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, originalVolume, 0);
                }
            });

            mediaPlayer.start();


            reference.child("Users").child(user.getPhoneNumber()).child("isCall").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        theirUserPhone = snapshot.getValue(String.class);
                        reference.child("Users").child(theirUserPhone).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                User theirUser = task.getResult().getValue(User.class);
                                assert theirUser != null;
                                name.setText(theirUser.getUserName());
                                Picasso.get().load(theirUser.getUserAvatar()).placeholder(R.drawable.profile).into(avatar);
                            }
                        });
                    } else {
                        if (call != null) {
                            call.hangup();
                        }
                        if(mediaPlayer != null) {
                            if(mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                        }
                        dialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            hangup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reference.child("Users").child(theirUserPhone).child("isCall").setValue(null);
                    reference.child("Users").child(user.getPhoneNumber()).child("isCall").setValue(null);
                    dialog.dismiss();
                    if (call != null) {
                        call.hangup();
                    }
                    if(mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                }
            });
            call_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mediaPlayer != null) {
                        if(mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                    }
                    activity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                    sinchClient.getAudioController().disableSpeaker();
                    call.answer();
                    call.addCallListener(new SinchCallListener());
                    call_start.setVisibility(View.GONE);
                    center_space.setVisibility(View.GONE);
                    status.setVisibility(View.GONE);
                    time.setVisibility(View.VISIBLE);
                    time.setBase(SystemClock.elapsedRealtime());
                    time.start();
                }
            });

            try {
                dialog.show();
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        reference.child("Users").child(theirUserPhone).child("isCall").setValue(null);
                        reference.child("Users").child(user.getPhoneNumber()).child("isCall").setValue(null);
                        dialog.dismiss();
                    }
                });
            } catch (Exception e) {
                return;
            }



        }
    }
}
