package com.nieldeokar.whatsappaudiorecorder.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class AudioRecordThread implements Runnable {
    private static final String TAG = AudioRecordThread.class.getSimpleName();


    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLE_RATE_INDEX = 4;
    private static final int CHANNELS = 1;
    private static final int BIT_RATE = 32000;

    private final int bufferSize;
    private final MediaCodec mediaCodec;
    private final AudioRecord audioRecord;
    private final OutputStream outputStream;

    private OnRecorderFailedListener onRecorderFailedListener;


    AudioRecordThread(OutputStream outputStream, OnRecorderFailedListener onRecorderFailedListener) throws IOException {

        this.bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        this.audioRecord = createAudioRecord(this.bufferSize);
        this.mediaCodec = createMediaCodec(this.bufferSize);
        this.outputStream = outputStream;
        this.onRecorderFailedListener = onRecorderFailedListener;

        this.mediaCodec.start();

        try {
            audioRecord.startRecording();
        } catch (Exception e) {
            Log.w(TAG, e);
            mediaCodec.release();
            throw new IOException(e);
        }
    }

    @Override
    public void run() {
        if (onRecorderFailedListener != null) {
            Log.d(TAG, "onRecorderStarted");
            onRecorderFailedListener.onRecorderStarted();
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//        byte[]                audioRecordData    = new byte[bufferSize];
        ByteBuffer[] codecInputBuffers = mediaCodec.getInputBuffers();
        ByteBuffer[] codecOutputBuffers = mediaCodec.getOutputBuffers();

        try {
            while (!Thread.interrupted()) {

//          handleCodecInput(audioRecord, audioRecordData, mediaCodec, codecInputBuffers, running);
            boolean success = handleCodecInput(audioRecord, mediaCodec, codecInputBuffers, Thread.currentThread().isAlive());
            if (success)
                handleCodecOutput(mediaCodec, codecOutputBuffers, bufferInfo, outputStream);
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            mediaCodec.stop();
            audioRecord.stop();

            mediaCodec.release();
            audioRecord.release();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean handleCodecInput(AudioRecord audioRecord,
                                     MediaCodec mediaCodec, ByteBuffer[] codecInputBuffers,
                                     boolean running) throws IOException {
        byte[] audioRecordData = new byte[bufferSize];
        int length = audioRecord.read(audioRecordData, 0, audioRecordData.length);

        if (length == AudioRecord.ERROR_BAD_VALUE ||
                length == AudioRecord.ERROR_INVALID_OPERATION ||
                length != bufferSize) {

            if (length != bufferSize) {
                if (onRecorderFailedListener != null) {
                    Log.d(TAG, "length != BufferSize calling onRecordFailedxx");
                    onRecorderFailedListener.onRecorderFailed();
                }
                return false;
            }
        }

//        Log.d(TAG, "Recording on  "+Thread.currentThread().getId() + " length "+ length);

        int codecInputBufferIndex = mediaCodec.dequeueInputBuffer(10 * 1000);

        if (codecInputBufferIndex >= 0) {
            ByteBuffer codecBuffer = codecInputBuffers[codecInputBufferIndex];
            codecBuffer.clear();
            codecBuffer.put(audioRecordData);
            mediaCodec.queueInputBuffer(codecInputBufferIndex, 0, length, 0, running ? 0 : MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        }

        return true;
    }

    private void handleCodecOutput(MediaCodec mediaCodec,
                                   ByteBuffer[] codecOutputBuffers,
                                   MediaCodec.BufferInfo bufferInfo,
                                   OutputStream outputStream)
            throws IOException {
        int codecOutputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

        while (codecOutputBufferIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
            if (codecOutputBufferIndex >= 0) {
                ByteBuffer encoderOutputBuffer = codecOutputBuffers[codecOutputBufferIndex];

                encoderOutputBuffer.position(bufferInfo.offset);
                encoderOutputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                    byte[] header = createAdtsHeader(bufferInfo.size - bufferInfo.offset);


                    outputStream.write(header);

                    byte[] data = new byte[encoderOutputBuffer.remaining()];
                    encoderOutputBuffer.get(data);
                    outputStream.write(data);
                }

                encoderOutputBuffer.clear();

                mediaCodec.releaseOutputBuffer(codecOutputBufferIndex, false);
            } else if (codecOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                codecOutputBuffers = mediaCodec.getOutputBuffers();
            }

            codecOutputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }


    private byte[] createAdtsHeader(int length) {
        int frameLength = length + 7;
        byte[] adtsHeader = new byte[7];

        adtsHeader[0] = (byte) 0xFF; // Sync Word
        adtsHeader[1] = (byte) 0xF1; // MPEG-4, Layer (0), No CRC
        adtsHeader[2] = (byte) ((MediaCodecInfo.CodecProfileLevel.AACObjectLC - 1) << 6);
        adtsHeader[2] |= (((byte) SAMPLE_RATE_INDEX) << 2);
        adtsHeader[2] |= (((byte) CHANNELS) >> 2);
        adtsHeader[3] = (byte) (((CHANNELS & 3) << 6) | ((frameLength >> 11) & 0x03));
        adtsHeader[4] = (byte) ((frameLength >> 3) & 0xFF);
        adtsHeader[5] = (byte) (((frameLength & 0x07) << 5) | 0x1f);
        adtsHeader[6] = (byte) 0xFC;

        return adtsHeader;
    }

    private AudioRecord createAudioRecord(int bufferSize) {
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize * 10);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.d(TAG, "Unable to initialze AudioRecord");
            throw new RuntimeException("Unable to initialize AudioRecord");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (android.media.audiofx.NoiseSuppressor.isAvailable()) {
                android.media.audiofx.NoiseSuppressor noiseSuppressor = android.media.audiofx.NoiseSuppressor
                        .create(audioRecord.getAudioSessionId());
                if (noiseSuppressor != null) {
                    noiseSuppressor.setEnabled(true);
                }
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (android.media.audiofx.AutomaticGainControl.isAvailable()) {
                android.media.audiofx.AutomaticGainControl automaticGainControl = android.media.audiofx.AutomaticGainControl
                        .create(audioRecord.getAudioSessionId());
                if (automaticGainControl != null) {
                    automaticGainControl.setEnabled(true);
                }
            }
        }


        return audioRecord;
    }

    private MediaCodec createMediaCodec(int bufferSize) throws IOException {
        MediaCodec mediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
        MediaFormat mediaFormat = new MediaFormat();

        mediaFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNELS);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        try {
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            Log.w(TAG, e);
            mediaCodec.release();
            throw new IOException(e);
        }

        return mediaCodec;
    }

    interface OnRecorderFailedListener {
        void onRecorderFailed();

        void onRecorderStarted();
    }
}
