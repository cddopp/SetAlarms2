package com.example.setalarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    MediaPlayer mp;

    @Override
    public void onReceive(Context context, Intent intent) {
        //mp=MediaPlayer.create(context, R.raw.alarm);
        mp = new MediaPlayer();
        mp.start();
        Toast.makeText(context, "Alarm is going off", Toast.LENGTH_LONG).show();
    }
}
