package com.nieldeokar.whatsappaudiorecorder.recorder;

import android.view.View;

/**
 * Created by Devlomi on 16/12/2017.
 */

public interface OnRecordClickListener {
    void onClick(View v);

    void askForVoicePermission();
}
