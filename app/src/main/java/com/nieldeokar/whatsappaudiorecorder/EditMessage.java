package com.nieldeokar.whatsappaudiorecorder;

import android.content.Context
import android.os.Handler
import android.util.AttributeSet

class EditMessage : android.support.v7.widget.AppCompatEditText {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    fun setKeyboardListener(listener: KeyboardListener?) {
        keyboardListener = listener
        if (listener != null) {
            isUserTyping = false
        }
    }

    public override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        if (mTypingHandler != null && keyboardListener != null) {
            mTypingHandler!!.removeCallbacks(mTypingTimeout)
            mTypingHandler!!.postDelayed(mTypingTimeout, TYPING_TIMEOUT.toLong())
            val length = text.toString().length
            if (!isUserTyping && length > 0) {
                isUserTyping = true
                keyboardListener!!.onTypingStarted()
                mTypingHandler!!.postDelayed(mTypingResend, 5000)
            } else if (isUserTyping && length == 0) {
                isUserTyping = false
                keyboardListener!!.onTextDeleted()
            }
        }
    }

    interface KeyboardListener {

        fun onTypingStarted()

        fun onTypingStopped()

        fun onTextDeleted()
    }

    companion object {

        val TYPING_TIMEOUT = 3000

        private var keyboardListener: KeyboardListener? = null
        private var mTypingHandler: Handler? = Handler()
        private var isUserTyping = false

        private var mTypingTimeout: Runnable = Runnable {
            if (isUserTyping && keyboardListener != null) {
                keyboardListener!!.onTypingStopped()
                isUserTyping = false
            }
        }

        private var mTypingResend: Runnable = Runnable {
            if (isUserTyping && keyboardListener != null) {
                keyboardListener!!.onTypingStarted()
            }
        }
    }
}
