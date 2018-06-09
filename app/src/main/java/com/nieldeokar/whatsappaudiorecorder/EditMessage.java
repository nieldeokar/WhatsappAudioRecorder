/*
 * Copyright (C) 2017.  QT-Software Ltd, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written and created by Shain Singh<support@qtsoftwareltd.net> on 11/7/17 10:20 AM
 */

package com.nieldeokar.whatsappaudiorecorder;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.vanniktech.emoji.EmojiEditText;

import java.util.Arrays;



public class EditMessage extends EmojiEditText {

    private static final String TAG = "EditMessage";
    public static final int TYPING_TIMEOUT = 3000;

    protected static Handler mTypingHandler = new Handler();
    protected static KeyboardListener keyboardListener;
    private static boolean isUserTyping = false;

    protected static Runnable mTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (isUserTyping && keyboardListener != null) {
                keyboardListener.onTypingStopped();
                isUserTyping = false;
            }
        }
    };

    protected static Runnable mTypingResend = new Runnable() {
        @Override
        public void run() {
            if (isUserTyping && keyboardListener != null) {
                keyboardListener.onTypingStarted();
            }
        }
    };
    private static int lengthWhenStopped = 0;
    private boolean lastInputWasTab = false;

    private InputContentInfoCompat mCurrentInputContentInfo;
    private int mCurrentFlags;
    private  String[] mimeTypes;  // our own copy of contentMimeTypes.

    public EditMessage(Context context) {
        super(context);
    }

    public EditMessage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRichContentType(String[] contentMimeTypes) {
        if (contentMimeTypes == null || contentMimeTypes.length == 0) {
            mimeTypes = new String[0];
        } else {
            mimeTypes = Arrays.copyOf(contentMimeTypes, contentMimeTypes.length);
        }
    }


    private void init() {
        Log.d(TAG, "init: " + TAG);
        this.setCustomSelectionActionModeCallback(new ActionModeCallbackInterceptor());
        this.setLongClickable(false);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && !e.isShiftPressed()) {
            lastInputWasTab = false;
            if (keyboardListener != null && keyboardListener.onEnterPressed()) {
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_TAB && !e.isAltPressed() && !e.isCtrlPressed()) {
            if (keyboardListener != null && keyboardListener.onTabPressed(this.lastInputWasTab)) {
                lastInputWasTab = true;
                return true;
            }
        } else {
            lastInputWasTab = false;
        }
        return super.onKeyDown(keyCode, e);
    }

   /* @Override
    public Editable getText() {
        return super.getText();
    }*/

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        lastInputWasTab = false;
        if (mTypingHandler != null && keyboardListener != null) {
            mTypingHandler.removeCallbacks(mTypingTimeout);
            mTypingHandler.postDelayed(mTypingTimeout, TYPING_TIMEOUT);
            final int length = text.toString().length();
            //keyboardListener.textLengthChanged(length);
//            lengthWhenStopped = length;
            if (!isUserTyping && length > 0) {
                isUserTyping = true;
                keyboardListener.onTypingStarted();
                mTypingHandler.postDelayed(mTypingResend, 5000);
            } else if (isUserTyping && length == 0) {
                isUserTyping = false;
                keyboardListener.onTextDeleted();
            }
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {

        final InputConnection ic = super.onCreateInputConnection(editorInfo);
        if(ic == null) return null;
        EditorInfoCompat.setContentMimeTypes(editorInfo, mimeTypes);
        final InputConnectionCompat.OnCommitContentListener callback =
                new InputConnectionCompat.OnCommitContentListener() {
                    @Override
                    public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                   int flags, Bundle opts) {
                        return EditMessage.this.onCommitContent(
                                inputContentInfo, flags, opts, mimeTypes);
                    }
                };
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
    }


    private boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                    Bundle opts, String[] contentMimeTypes) {
        // Clear the temporary permission (if any).  See below about why we do this here.
        try {
            if (mCurrentInputContentInfo != null) {
                mCurrentInputContentInfo.releasePermission();
            }
        } catch (Exception e) {
            Log.e("Editmessage","InputContentInfoCompat#releasePermission() failed."+e);
        } finally {
            mCurrentInputContentInfo = null;
        }


        boolean supported = false;
        for (final String mimeType : contentMimeTypes) {
            if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
                supported = true;
                break;
            }
        }
        if (!supported) {
            return false;
        }

        return onCommitContentInternal(inputContentInfo, flags);
    }

    private boolean onCommitContentInternal(InputContentInfoCompat inputContentInfo, int flags) {
        if ((flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                inputContentInfo.requestPermission();
            } catch (Exception e) {
                Log.e("Editmessage","InputContentInfoCompat#requestPermission() failed.");
                return false;
            }
        }

        Uri linkUri = inputContentInfo.getLinkUri();
        keyboardListener.sendKeyboardRichContent(linkUri);
        // Due to the asynchronous nature of WebView, it is a bit too early to call
        // inputContentInfo.releasePermission() here. Hence we call IC#releasePermission() when this
        // method is called next time.  Note that calling IC#releasePermission() is just to be a
        // good citizen. Even if we failed to call that method, the system would eventually revoke
        // the permission sometime after inputContentInfo object gets garbage-collected.
        mCurrentInputContentInfo = inputContentInfo;
        mCurrentFlags = flags;

        return true;
    }


    public void setKeyboardListener(KeyboardListener listener) {
        keyboardListener = listener;
        if (listener != null) {
            isUserTyping = false;
        }
    }

    public interface KeyboardListener {
        boolean onEnterPressed();

        void onTypingStarted();

        void onTypingStopped();

        void onTextDeleted();

        void textLengthChanged(int length);

        boolean onTabPressed(boolean repeated);

        void sendKeyboardRichContent(Uri uri);
    }

    private class ActionModeCallbackInterceptor implements ActionMode.Callback {
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }
    }

}
