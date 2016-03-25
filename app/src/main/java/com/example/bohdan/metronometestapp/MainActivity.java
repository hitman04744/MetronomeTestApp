package com.example.bohdan.metronometestapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    boolean isRunning = false;
    private LinearLayout mVibroButton;
    private LinearLayout mSoundButton;
    private LinearLayout mFlashdButton;
    private ImageView mImageView;
    private ImageView mFlashImageView;
    private ImageView mVibroImageView;
    private ImageView mSoundImageView;
    private TextView mFlashText;
    private TextView mVibroText;
    private TextView mSoundText;
    private SeekBar mySeekBar;
    private Button mStartButton;
    private TextView mText;
    private boolean state = false;
    private boolean color = true;
    private int usersAccess;
    private int vibroState = 4;
    private int flashState = 2;
    private int soundState = 1;
    private int bpm = 60;
    private long time;
    private SoundPool spool;
    private int repeatSound;
    private ScheduledExecutorService scheduleTaskExecutor;
    private Runnable myTask;
    private ScheduledFuture<?> futureTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        myTask = new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                choice();
                runOnUiThread(new Runnable() {
                    public void run() {
                        ledIndicator();
                    }
                });
            }
        };
        isRunning = false;
    }


    public void findViews() {
        //buttons
        mVibroButton = (LinearLayout) findViewById(R.id.vibroButton);
        mSoundButton = (LinearLayout) findViewById(R.id.soundButton);
        mFlashdButton = (LinearLayout) findViewById(R.id.flashLightButton);
        mStartButton = (Button) findViewById(R.id.button);
        mStartButton.setOnClickListener(this);
        mVibroButton.setOnClickListener(this);
        mSoundButton.setOnClickListener(this);
        mFlashdButton.setOnClickListener(this);
        //buttonViews
        mFlashImageView = (ImageView) findViewById(R.id.flashImage);
        mVibroImageView = (ImageView) findViewById(R.id.vibroImage);
        mSoundImageView = (ImageView) findViewById(R.id.soundImage);
        mFlashText = (TextView) findViewById(R.id.flashText);
        mVibroText = (TextView) findViewById(R.id.vibroText);
        mSoundText = (TextView) findViewById(R.id.soundText);
        mImageView = (ImageView) findViewById(R.id.ledImageView);
        //Other views
        mText = (TextView) findViewById(R.id.editText);
        mySeekBar = (SeekBar) findViewById(R.id.seekBar);
        mStartButton = (Button) findViewById(R.id.button);
        mySeekBar.setProgress(bpm);
        mImageView.setImageResource(R.drawable.led_off);
        mySeekBar.setProgress(bpm);
        mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpm = progress;
                mText.setText(bpm + " bpm");
                if (isRunning) {
                    bpm = progress;
                    time = 60000 / bpm;
                    changeReadInterval(time);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                state = false;
                spool.autoPause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                state = true;
                spool.autoResume();
            }
        });
        mText.setText(bpm + " bpm");
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


    }

    public void vibro() {
        Log.d("My", "Vibro");
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
        if (state) {
            spool.play(repeatSound, 1, 1, 0, 0, 1);
        }
    }

    public void ledIndicator() {
        if (color) {
            mImageView.setImageResource(R.drawable.led_off);
            color = false;
        } else {
            mImageView.setImageResource(R.drawable.led_on);
            color = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.vibroButton)://4, 100
                if (usersAccess < 4) {
                    usersAccess = usersAccess | vibroState;
                    mVibroText.setTextColor(getResources().getColor(R.color.state_ON));
                } else {
                    mVibroText.setTextColor(getResources().getColor(R.color.state_OFF));
                    usersAccess = usersAccess ^ vibroState;
                }
                break;
            case (R.id.flashLightButton): //2, 010
                if (usersAccess < 2 || usersAccess < 6) {
                    usersAccess = usersAccess | flashState;
                    mFlashText.setTextColor(getResources().getColor(R.color.state_ON));
                } else {
                    mFlashText.setTextColor(getResources().getColor(R.color.state_OFF));
                    usersAccess = usersAccess ^ flashState;
                }
                break;
            case (R.id.soundButton): //1, 001
                if (usersAccess % 2 == 0) {
                    usersAccess = usersAccess | soundState;
                    mSoundText.setTextColor(getResources().getColor(R.color.state_ON));
                } else {
                    mSoundText.setTextColor(getResources().getColor(R.color.state_OFF));
                    usersAccess = usersAccess ^ soundState;
                }
                break;
            case (R.id.button):
                if (mStartButton.getText().equals("Start")) {
                    time = 60000 / bpm;
                    futureTask = scheduleTaskExecutor.scheduleAtFixedRate(myTask, 0, time, TimeUnit.MILLISECONDS);
                    mStartButton.setText("Stop");
                } else {
                    futureTask.cancel(true);
                    mStartButton.setText("Start");
                }
                break;
        }
    }

    public void choice() {
        switch (usersAccess) {
            case (0):
                ledIndicator();
                Toast.makeText(this, "choice " + usersAccess + "", Toast.LENGTH_SHORT).show();
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

    public void changeReadInterval(long time) {
        if (futureTask != null) {
            futureTask.cancel(true);
        }
        futureTask = scheduleTaskExecutor.scheduleAtFixedRate(myTask, 0, time, TimeUnit.MILLISECONDS);
        Log.d("Time", time + "");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(MainActivity.this, MyService.class);
        intent.putExtra("time", time);
        intent.putExtra("userAccess", usersAccess);
        intent.putExtra("sound", repeatSound);
        startService(intent);
        futureTask.cancel(true);
//
    }
}
