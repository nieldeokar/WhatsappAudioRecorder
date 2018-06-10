package com.nieldeokar.whatsappaudiorecorder.recorder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nieldeokar.whatsappaudiorecorder.R;

/**
 * Created by Devlomi on 13/12/2017.
 */

public class RecordButton extends android.support.v7.widget.AppCompatImageView implements View.OnTouchListener, View.OnClickListener {

    private ScaleAnim scaleAnim;
    private RecordView recordView;
    private boolean listenForRecord = true;
    private OnRecordClickListener onRecordClickListener;
    Context context;

    private boolean isTextMessage = false;

    public void changeToMessage(boolean flag) {
        this.isTextMessage = flag;
        setListenForRecord(!flag);
        if(flag)
            setImageResource(R.drawable.ic_send_24dp);
        else
            setImageResource(R.drawable.ic_mic_black_24dp);
    }


    public boolean isTextMessage(){
        return isTextMessage;
    }

    public void setRecordView(RecordView recordView) {
        this.recordView = recordView;
    }

    public RecordButton(Context context) {
        super(context);
        init(context, null);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordButton);

            int imageResource = typedArray.getResourceId(R.styleable.RecordButton_src, -1);


            if (imageResource != -1) {
                setTheImageResource(imageResource);
            }

            typedArray.recycle();
        }
        this.context = context;

        scaleAnim = new ScaleAnim(this);
        this.setOnTouchListener(this);
        this.setOnClickListener(this);


    }


    private void setTheImageResource(int imageResource) {
        Drawable image = AppCompatResources.getDrawable(getContext(), imageResource);
        setImageDrawable(image);
    }

    @Override
    public synchronized boolean onTouch(View v, MotionEvent event) {
        if (isListenForRecord()) {

            if(!hasVoicePermission()){
                onRecordClickListener.askForVoicePermission();
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundResource(R.drawable.bg_mic);
                    vibrate();
                    recordView.setVisibility(VISIBLE);
                    recordView.onActionDown((RecordButton) v, event);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    recordView.onActionMove((RecordButton) v, event);
                    break;

                case MotionEvent.ACTION_UP:
                    v.setBackgroundResource(0);
                    recordView.onActionUp((RecordButton) v);
                    break;
                default:
                    return false;

            }
        }
        return isListenForRecord();

    }



    public void vibrate() {
        if(context == null) return;
        long timeInMillis = 50L;
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createOneShot(timeInMillis, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(timeInMillis);
        }
    }

    protected void startScale() {
        scaleAnim.start();
    }

    protected void stopScale() {
        scaleAnim.stop();
    }

    public void setListenForRecord(boolean listenForRecord) {
        this.listenForRecord = listenForRecord;
    }

    public boolean isListenForRecord() {
        return listenForRecord;
    }

    public void setOnRecordClickListener(OnRecordClickListener onRecordClickListener) {
        this.onRecordClickListener = onRecordClickListener;
    }


    @Override
    public void onClick(View v) {
        if (onRecordClickListener != null)
            onRecordClickListener.onClick(v);
    }

    private boolean hasVoicePermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

}

