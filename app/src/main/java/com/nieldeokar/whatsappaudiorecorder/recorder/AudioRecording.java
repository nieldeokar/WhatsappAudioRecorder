package com.nieldeokar.whatsappaudiorecorder.recorder;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * @author netodevel
 */
public class AudioRecording {

    private String mFileName;
    private Context mContext;

    private MediaPlayer mMediaPlayer;
    private OnAudioRecordListener onAudioRecordListener;
    private MediaRecorder mRecorder;
    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private File audioDirectory;

    public AudioRecording(Context context) {
        this.mContext = context;
    }

    AudioRecording(Context context, OnAudioRecordListener onAudioRecordListener) {
        this.mContext = context;
        this.onAudioRecordListener = onAudioRecordListener;
    }

    public AudioRecording() {
    }

    public AudioRecording setNameFile(String nameFile) {
        this.mFileName = nameFile;
        return this;
    }

    public synchronized AudioRecording start() {
        mRecorder = new MediaRecorder();

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(audioDirectory + mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
        } catch (Exception e) {
            this.onAudioRecordListener.onError(1);
        }
        return this;
    }

    public synchronized void stop(Boolean cancel) {
        try {
            if(mRecorder == null) return;
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
            mRecorder = null;

            RecordingItem recordingItem = new RecordingItem();
            recordingItem.setFilePath(audioDirectory + mFileName);
            recordingItem.setName(mFileName);
            recordingItem.setLength((int) mElapsedMillis);
            recordingItem.setTime(System.currentTimeMillis());

            if (!cancel) {
                onAudioRecordListener.onRecordFinished(recordingItem);
            } else {
                File file = new File(recordingItem.getFilePath());
                if (file != null && file.exists()) file.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void play(RecordingItem recordingItem) {
        try {
            this.mMediaPlayer = new MediaPlayer();
            this.mMediaPlayer.setDataSource(recordingItem.getFilePath());
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void releaseResourse(){
        try {
            if(mRecorder == null) return;
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setAudioDirectory(File audioDirectory){
        this.audioDirectory = audioDirectory;
    }

}
