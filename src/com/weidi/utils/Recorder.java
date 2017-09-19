/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (c) 2012, The Linux Foundation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.weidi.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 *
 */
public class Recorder {

    static final String TAG = "Recorder";
    static final String SAMPLE_PREFIX = "recording";
    static final String SAMPLE_PATH_KEY = "sample_path";
    static final String SAMPLE_LENGTH_KEY = "sample_length";

    // getCurrentState
    public static final int IDLE_STATE = 0;
    public static final int RECORDING_STATE = 1;
    public static final int PLAYING_STATE = 2;

    private int mCurrentState = IDLE_STATE;

    // error
    public static final int NO_ERROR = 0;
    public static final int SDCARD_ACCESS_ERROR = 1;
    public static final int INTERNAL_ERROR = 2;
    public static final int IN_CALL_RECORD_ERROR = 3;
    public static final int UNSUPPORTED_FORMAT = 4;

    public int mChannels = 0;
    // 采样速率
    public int mSamplingRate = 0;

    public interface OnStateChangedListener {
        public void onStateChanged(int state);

        public void onError(int error);
    }

    private OnStateChangedListener mOnStateChangedListener = null;

    private long mSampleStart = 0;       // time at which latest record or play operation started
    private int mSampleLength = 0;      // length of current sample
    private File mOutputFile = null;
//    private String mOutputFilePath = null;
    private FileDescriptor mOutputFileDescriptor = null;

    private MediaRecorder mMediaRecorder = null;
    private MediaPlayer mMediaPlayer = null;

    //
    private int mAudioSource;
    private int mAudioChannels;
    private int mAudioSamplingRate;
    private int mOutputFormat;
    private int mAudioEncoder;

    private int mAudioEncodingBitRate;

    public Recorder() {
    }

    public boolean startRecording() {
        if(!initMediaRecorder()){
            return false;
        }
        if(!prepare()){
            return false;
        }
        if(!start()){
            return false;
        }
        mSampleStart = System.currentTimeMillis();
        setState(RECORDING_STATE);
        return true;
    }

    public void startRecording(
            int outputfileformat,
            String extension,
            Context context,
            int audiosourcetype,
            int codectype) {

        stopRecording();

        if (mOutputFile == null) {
            File sampleDir = Environment.getExternalStorageDirectory();
            if (!sampleDir.canWrite()) // Workaround for broken sdcard support on the device.
                sampleDir = new File("/sdcard/sdcard");

            try {
                mOutputFile = File.createTempFile(SAMPLE_PREFIX, extension, sampleDir);
            } catch (IOException e) {
                setError(SDCARD_ACCESS_ERROR);
                return;
            }
        }

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setOnInfoListener(mOnInfoListener);
        mMediaRecorder.setAudioSource(audiosourcetype);
        //set channel for surround sound recording.
        if (mChannels > 0) {
            mMediaRecorder.setAudioChannels(mChannels);
        }
        if (mSamplingRate > 0) {
            mMediaRecorder.setAudioSamplingRate(mSamplingRate);
        }

        mMediaRecorder.setOutputFormat(outputfileformat);

        try {
            mMediaRecorder.setAudioEncoder(codectype);
        } catch (RuntimeException exception) {
            setError(UNSUPPORTED_FORMAT);
            mMediaRecorder.reset();
            mMediaRecorder.release();
            if (mOutputFile != null) {
                mOutputFile.delete();
            }
            mOutputFile = null;
            mSampleLength = 0;
            mMediaRecorder = null;
            return;
        }

        mMediaRecorder.setOutputFile(mOutputFile.getAbsolutePath());

        // Handle IOException
        try {
            mMediaRecorder.prepare();
        } catch (IOException exception) {
            setError(INTERNAL_ERROR);
            mMediaRecorder.reset();
            mMediaRecorder.release();
            if (mOutputFile != null)
                mOutputFile.delete();
            mOutputFile = null;
            mSampleLength = 0;
            mMediaRecorder = null;
            return;
        }
        // Handle RuntimeException if the recording couldn't start
        Log.e(TAG, "audiosourcetype " + audiosourcetype);
        try {
            mMediaRecorder.start();
        } catch (RuntimeException exception) {
            AudioManager audioMngr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            boolean isInCall = ((audioMngr.getMode() == AudioManager.MODE_IN_CALL) ||
                    (audioMngr.getMode() == AudioManager.MODE_IN_COMMUNICATION));
            if (isInCall) {
                setError(IN_CALL_RECORD_ERROR);
            } else {
                setError(INTERNAL_ERROR);
            }
            mMediaRecorder.reset();
            mMediaRecorder.release();
            if (mOutputFile != null)
                mOutputFile.delete();
            mOutputFile = null;
            mSampleLength = 0;
            mMediaRecorder = null;
            return;
        }
        mSampleStart = System.currentTimeMillis();
        setState(RECORDING_STATE);
    }

    public void pauseRecording() {
        //        pause();
    }

    public boolean stopRecording() {
        if(!stop()){
            return false;
        }
        if(!reset()){
            return false;
        }
        if(!release()){
            return false;
        }

        mSampleLength = (int) ((System.currentTimeMillis() - mSampleStart) / 1000);
        setState(IDLE_STATE);
        return true;
    }

    public void saveState(Bundle recorderState) {
        recorderState.putString(SAMPLE_PATH_KEY, mOutputFile.getAbsolutePath());
        recorderState.putInt(SAMPLE_LENGTH_KEY, mSampleLength);
    }

    public int getMaxAmplitude() {
        if (mCurrentState != RECORDING_STATE)
            return 0;
        return mMediaRecorder.getMaxAmplitude();
    }

    public void restoreState(Bundle recorderState) {
        String samplePath = recorderState.getString(SAMPLE_PATH_KEY);
        if (samplePath == null)
            return;
        int sampleLength = recorderState.getInt(SAMPLE_LENGTH_KEY, -1);
        if (sampleLength == -1)
            return;

        File file = new File(samplePath);
        if (!file.exists())
            return;
        if (mOutputFile != null
                && mOutputFile.getAbsolutePath().compareTo(file.getAbsolutePath()) == 0)
            return;

        delete();
        mOutputFile = file;
        mSampleLength = sampleLength;

        onStateChanged(IDLE_STATE);
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mOnStateChangedListener = listener;
    }

    public void setChannels(int nChannelsCount) {
        mChannels = nChannelsCount;
    }

    public void setSamplingRate(int samplingRate) {
        mSamplingRate = samplingRate;
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    public int progress() {
        if (mCurrentState == RECORDING_STATE || mCurrentState == PLAYING_STATE) {
            return (int) ((System.currentTimeMillis() - mSampleStart) / 1000);
        }
        return 0;
    }

    public int sampleLength() {
        return mSampleLength;
    }

    public File sampleFile() {
        return mOutputFile;
    }

    /**
     * Resets the recorder getCurrentState. If a sample was recorded, the file is deleted.
     */
    public void delete() {
        stop();

        if (mOutputFile != null) {
            mOutputFile.delete();
        }

        mOutputFile = null;
        mSampleLength = 0;

        onStateChanged(IDLE_STATE);
    }

    /**
     * Resets the recorder getCurrentState. If a sample was recorded, the file is left on disk
     * and will
     * be reused for a new recording.
     */
    public void clear() {
        stop();

        mSampleLength = 0;

        onStateChanged(IDLE_STATE);
    }

    public void startPlayback() {
        stop();

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mOutputFile.getAbsolutePath());
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            setError(INTERNAL_ERROR);
            mMediaPlayer = null;
            return;
        } catch (IOException e) {
            setError(SDCARD_ACCESS_ERROR);
            mMediaPlayer = null;
            return;
        }

        mSampleStart = System.currentTimeMillis();
        setState(PLAYING_STATE);
    }

    public void stopPlayback() {
        if (mMediaPlayer == null) // we were not in playback
            return;

        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        setState(IDLE_STATE);
    }

    public int getAudioSource() {
        return mAudioSource;
    }

    public Recorder setAudioSource(int mAudioSource) {
        this.mAudioSource = mAudioSource;
        return this;
    }

    public int getAudioChannels() {
        return mAudioChannels;
    }

    public Recorder setAudioChannels(int mAudioChannels) {
        this.mAudioChannels = mAudioChannels;
        return this;
    }

    public int getAudioSamplingRate() {
        return mAudioSamplingRate;
    }

    public Recorder setAudioSamplingRate(int mAudioSamplingRate) {
        this.mAudioSamplingRate = mAudioSamplingRate;
        return this;
    }

    public int getOutputFormat() {
        return mOutputFormat;
    }

    public Recorder setOutputFormat(int mOutputFormat) {
        this.mOutputFormat = mOutputFormat;
        return this;
    }

    public int getAudioEncoder() {
        return mAudioEncoder;
    }

    public Recorder setAudioEncoder(int mAudioEncoder) {
        this.mAudioEncoder = mAudioEncoder;
        return this;
    }

    public FileDescriptor getOutputFileDescriptor() {
        return mOutputFileDescriptor;
    }

    public Recorder setOutputFileDescriptor(FileDescriptor mOutputFile) {
        this.mOutputFileDescriptor = mOutputFile;
        return this;
    }

    public File getOutputFile() {
        return mOutputFile;
    }

    public int getAudioEncodingBitRate() {
        return mAudioEncodingBitRate;
    }

    public void setAudioEncodingBitRate(int mAudioEncodingBitRate) {
        this.mAudioEncodingBitRate = mAudioEncodingBitRate;
    }

    public Recorder setOutputFile(File mOutputFile) {
        this.mOutputFile = mOutputFile;
        return this;
    }

    private boolean initMediaRecorder() {
        try {
            mMediaRecorder = new MediaRecorder();
            // 千万注意设置的顺序
            mMediaRecorder.setAudioSource(mAudioSource);
            if (mChannels > 0) {
                mMediaRecorder.setAudioChannels(mAudioChannels);
            }
            if(mAudioEncodingBitRate > 0){
                mMediaRecorder.setAudioEncodingBitRate(mAudioEncodingBitRate);
            }
            if (mSamplingRate > 0) {
                mMediaRecorder.setAudioSamplingRate(mAudioSamplingRate);
            }

            mMediaRecorder.setOutputFormat(mOutputFormat);

            try {
                mMediaRecorder.setAudioEncoder(mAudioEncoder);
            } catch (RuntimeException exception) {
                setError(UNSUPPORTED_FORMAT);
                mMediaRecorder.reset();
                mMediaRecorder.release();
                if (mOutputFile != null) {
                    mOutputFile.delete();
                }
                mOutputFile = null;
                mSampleLength = 0;
                mMediaRecorder = null;
                return false;
            }

            //            mMediaRecorder.setOutputFile(mOutputFilePath);
            mMediaRecorder.setOutputFile(mOutputFile.getAbsolutePath());

            mMediaRecorder.setOnInfoListener(mOnInfoListener);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean prepare() {
        if (mMediaRecorder == null) {
            if (!initMediaRecorder()) {
                return false;
            }
        }
        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            setError(INTERNAL_ERROR);
            mMediaRecorder.reset();
            mMediaRecorder.release();
            if (mOutputFile != null) {
                mOutputFile.delete();
            }
            mOutputFile = null;
            mSampleLength = 0;
            mMediaRecorder = null;
            return false;
        }
        return true;
    }

    private boolean start() {
        if (mMediaRecorder == null) {
            return false;
        }
        try {
            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //    private void pause() {
    //        if (mMediaRecorder == null) {
    //            return;
    //        }
    //        try {
    //            mMediaRecorder.pause();
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    }

    private boolean stop() {
        if (mMediaRecorder == null) {
            return false;
        }
        try {
            mMediaRecorder.stop();
        } catch (Exception e) {
            e.printStackTrace();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            return false;
        }
        return true;
        //        stopRecording();
        //        stopPlayback();
    }

    private boolean reset() {
        if (mMediaRecorder == null) {
            return false;
        }
        try {
            mMediaRecorder.reset();
        } catch (Exception e) {
            e.printStackTrace();
            mMediaRecorder.release();
            mMediaRecorder = null;
            return false;
        }
        return true;
    }

    private boolean release() {
        if (mMediaRecorder == null) {
            return false;
        }
        try {
            mMediaRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
            mMediaRecorder = null;
            return false;
        }
        mMediaRecorder = null;
        return true;
    }

    private void setState(int state) {
        if (state == mCurrentState)
            return;

        mCurrentState = state;
        onStateChanged(mCurrentState);
    }

    private void onStateChanged(int state) {
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onStateChanged(state);
        }
    }

    private void setError(int error) {
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onError(error);
        }
    }

    private OnErrorListener mOnErrorListener = new OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            stop();
            reset();
            release();
            setError(SDCARD_ACCESS_ERROR);
            return true;
        }

    };

    private MediaRecorder.OnInfoListener mOnInfoListener = new MediaRecorder.OnInfoListener() {

        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {

        }

    };

    private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            stop();
        }

    };

}
