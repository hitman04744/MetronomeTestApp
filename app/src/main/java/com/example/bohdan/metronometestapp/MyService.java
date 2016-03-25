package com.example.bohdan.metronometestapp;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    boolean isRunning = false;
    private ScheduledExecutorService scheduleTaskExecutor;
    private Runnable myTask;
    private long time;
    private int usersAccess;
    private SoundPool spool;
    private int repeatSound;

    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        time = intent.getLongExtra("time", 1);
        usersAccess = intent.getIntExtra("userAccess", 0);

        spool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
// Sound pool on load complete listener
        spool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                Log.i("OnLoadCompleteListener", "Sound " + sampleId + " loaded.");
                boolean loaded = true;

            }
        });
        repeatSound = spool.load(getApplicationContext(), R.raw.pop, 1);
        Log.d("My", "Service started");
        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        myTask = new Runnable() {
            @Override
            public void run() {
                choice();
            }
        };

        scheduleTaskExecutor.scheduleAtFixedRate(myTask, 0, time, TimeUnit.MILLISECONDS);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "destr", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void choice() {
        switch (usersAccess) {
            case (0):
                break;
            case (1):
                sound();
                break;
            case (2):
                flash();
                break;
            case (3):
                flash();
                sound();
                break;
            case (4):
                vibro();
                break;
            case (5):
                vibro();
                sound();
                break;
            case (6):
                vibro();
                flash();
                break;
            case (7):
                vibro();
                sound();
                flash();
                break;
        }
    }

    public void vibro() {
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(time / 10);
    }

    public void flash() {
        Camera cam = Camera.open();
        Camera.Parameters p = cam.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cam.setParameters(p);
        cam.release();
    }

    public void sound() {
        spool.play(repeatSound, 1, 1, 0, 0, 1);

    }


}