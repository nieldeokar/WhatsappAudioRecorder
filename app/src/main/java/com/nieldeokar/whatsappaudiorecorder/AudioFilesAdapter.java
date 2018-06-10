package com.nieldeokar.whatsappaudiorecorder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nieldeokar.whatsappaudiorecorder.recorder.RecordingItem;

import java.util.List;


/**
 * Created by @nieldeokar on 27/05/18.
 */

public class AudioFilesAdapter extends RecyclerView.Adapter<AudioFilesAdapter.MyViewHolder> {

    public List<RecordingItem> audioFilesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPlayTime, tvTotalPlayTime;
        public SeekBar mSeekbar;
        public ImageButton imgPlayPause;

        public MyViewHolder(View view) {
            super(view);
            tvPlayTime = (TextView) view.findViewById(R.id.tvPlaytime);
            tvTotalPlayTime = (TextView) view.findViewById(R.id.tvTotalPlayTime);
            mSeekbar = (SeekBar) view.findViewById(R.id.seekBar);
            imgPlayPause = (ImageButton) view.findViewById(R.id.imgPlay);
        }
    }


    public AudioFilesAdapter(List<RecordingItem> audioFiles) {
        this.audioFilesList = audioFiles;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_audio, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RecordingItem patientEntity = audioFilesList.get(position);

        holder.tvTotalPlayTime.setText(patientEntity.getTime());

    }

    @Override
    public int getItemCount() {
        return audioFilesList.size();
    }

}