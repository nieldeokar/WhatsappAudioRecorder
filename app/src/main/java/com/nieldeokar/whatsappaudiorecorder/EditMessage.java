package com.nieldeokar.whatsappaudiorecorder;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

public class EditMessage extends android.support.v7.widget.AppCompatEditText {

    public static final int TYPING_TIMEOUT = 3000;

    protected static KeyboardListener keyboardListener;
    protected static Handler mTypingHandler = new Handler();
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

    public EditMessage(Context context) {
        super(context);
    }

    public EditMessage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditMessage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setKeyboardListener(KeyboardListener listener) {
        keyboardListener = listener;
        if (listener != null) {
            isUserTyping = false;
        }
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        if (mTypingHandler != null && keyboardListener != null) {
            mTypingHandler.removeCallbacks(mTypingTimeout);
            mTypingHandler.postDelayed(mTypingTimeout, TYPING_TIMEOUT);
            final int length = text.toString().length();
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

    public interface KeyboardListener {

        void onTypingStarted();

        void onTypingStopped();

        void onTextDeleted();
    }
}
