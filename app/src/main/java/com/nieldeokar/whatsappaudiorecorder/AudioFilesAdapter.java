package com.nieldeokar.whatsappaudiorecorder;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nieldeokar.whatsappaudiorecorder.recorder.RecordingItem;

import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * Created by @nieldeokar on 27/05/18.
 */

public class AudioFilesAdapter extends RecyclerView.Adapter<AudioFilesAdapter.MyViewHolder> implements Handler.Callback {

    private List<RecordingItem> mRecordingItems;
    private Context mContext;

    private static final int MSG_UPDATE_SEEK_BAR = 1845;
    private MediaPlayer mediaPlayer;
    private Handler uiUpdateHandler = new Handler(this);
    private int mPlayingPosition = -1;
    private MyViewHolder mAudioPlayingHolder;

    public AudioFilesAdapter(List<RecordingItem> audioFiles, Context context) {
        this.mRecordingItems = audioFiles;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_audio, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RecordingItem recordingItem = mRecordingItems.get(position);

        holder.imgPlayPause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        holder.tvPlayTime.setText(Utils.parseTime(recordingItem.getLength()));
        holder.tvFilePath.setText(recordingItem.getFilePath());
        if (position == mPlayingPosition) {
            mAudioPlayingHolder = holder;
            updatePlayingView();
        } else {
            updateInitialPlayerView(holder);
        }

    }

    @Override
    public int getItemCount() {
        return mRecordingItems.size();
    }

    private void updatePlayingView() {
        if (mediaPlayer == null || mAudioPlayingHolder == null) return;
        mAudioPlayingHolder.audioSeekBar.setProgress(mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration());

        if (mediaPlayer.isPlaying()) {
            uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
            mAudioPlayingHolder.imgPlayPause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);

        } else {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
            mAudioPlayingHolder.imgPlayPause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        }
        mAudioPlayingHolder.tvPlayTime.setText(Utils.parseTime(mediaPlayer.getCurrentPosition()));
    }

    private void updateNonPlayingView(MyViewHolder holder) {
        if (holder == mAudioPlayingHolder) {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        }
        holder.audioSeekBar.setProgress(0);
        holder.imgPlayPause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
    }


    private void updateInitialPlayerView(MyViewHolder holder) {
        if (holder == mAudioPlayingHolder) {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        }
        holder.audioSeekBar.setProgress(0);
        holder.imgPlayPause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
    }


    @Override
    public boolean handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_SEEK_BAR: {
                int percentage = mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration();
                mAudioPlayingHolder.audioSeekBar.setProgress(percentage);
                mAudioPlayingHolder.tvPlayTime.setText(Utils.parseTime(mediaPlayer.getCurrentPosition()));
                uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
                return true;
            }
        }
        return false;
    }

    private void performPlayButtonClick(RecordingItem recordingItem, MyViewHolder myViewHolder) {

        int currentPosition = mRecordingItems.indexOf(recordingItem);
        if (currentPosition == mPlayingPosition) {
            // toggle between play/pause of audio
            if (mediaPlayer == null) return;
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        } else {
            // start another audio playback
            RecordingItem previousPlayObject = mPlayingPosition == -1 ? null : mRecordingItems.get(mPlayingPosition);
            mPlayingPosition = currentPosition;
            if (mediaPlayer != null) {
                if (null != mAudioPlayingHolder) {
                    if (previousPlayObject != null)
                        mAudioPlayingHolder.tvPlayTime.setText(Utils.parseTime(previousPlayObject.getLength()));
                    updateNonPlayingView(mAudioPlayingHolder);
                }
                mediaPlayer.release();
            }
            mAudioPlayingHolder = myViewHolder;
            startMediaPlayer(recordingItem);
        }
        updatePlayingView();
    }


    public void stopPlayer() {
        if (null != mediaPlayer) {
            releaseMediaPlayer();
        }
    }

    private void startMediaPlayer(RecordingItem recordingItem) {
        try {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer = MediaPlayer.create(mContext, Uri.parse(recordingItem.getFilePath()));
            } catch (Exception e) {
                e.printStackTrace();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(recordingItem.getFilePath());
            }
            if (mediaPlayer == null) return;
            mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
            if (mAudioPlayingHolder != null)
                //mediaPlayer.seekTo(mAudioPlayingHolder.audioSeekBar.getProgress());
                mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseMediaPlayer() {
        if (null != mAudioPlayingHolder) {
            updateNonPlayingView(mAudioPlayingHolder);
        }
        if (null != mediaPlayer) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mPlayingPosition = -1;
    }

    public void addItem(@Nullable RecordingItem recordingItem){
        if(recordingItem == null) return;
        mRecordingItems.add(recordingItem);
        notifyItemInserted(mRecordingItems.size());
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
        TextView tvPlayTime, tvFilePath;
        SeekBar audioSeekBar;
        ImageView imgPlayPause;

        MyViewHolder(View view) {
            super(view);
            tvPlayTime = view.findViewById(R.id.tvPlaytime);
            tvFilePath = view.findViewById(R.id.filePath);
            audioSeekBar = view.findViewById(R.id.seekBar);
            imgPlayPause = view.findViewById(R.id.imgPlay);

            audioSeekBar.setOnSeekBarChangeListener(this);
            imgPlayPause.setOnClickListener(this);
            imgPlayPause.setTag(this);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && mediaPlayer != null && getAdapterPosition() == mPlayingPosition)
                mediaPlayer.seekTo(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onClick(View view) {
            RecordingItem recordingItem = mRecordingItems.get(getAdapterPosition());
            if (view.getId() == R.id.imgPlay) {
                performPlayButtonClick(recordingItem, (MyViewHolder) view.getTag());
            }
        }
    }
}