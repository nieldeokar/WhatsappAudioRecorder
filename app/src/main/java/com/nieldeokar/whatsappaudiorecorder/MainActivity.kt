package com.nieldeokar.whatsappaudiorecorder

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.nieldeokar.whatsappaudiorecorder.recorder.OnRecordClickListener
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Environment.getExternalStorageDirectory
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.nieldeokar.whatsappaudiorecorder.recorder.OnAudioRecordListener
import com.nieldeokar.whatsappaudiorecorder.recorder.RecordingItem
import kotlinx.android.synthetic.main.editor.*
import java.io.File
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.nieldeokar.whatsappaudiorecorder.DividerItemDecoration.VERTICAL_LIST
import com.nieldeokar.whatsappaudiorecorder.recorder.QTAudioRecording


class MainActivity : AppCompatActivity(), OnRecordClickListener, EditMessage.KeyboardListener {

    companion object {
        const val VOICE_PERMISSION_REQUEST_CODE = 100
        const val STORAGE_PERMISSION_REQUEST_CODE = 101
        const val TAG = "MAIN"
    }

    lateinit var mAdapter : AudioFilesAdapter
    private var mRecordingItemsList = ArrayList<RecordingItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(hasExternalReadWritePermission()){
            val external = File(getExternalStorageDirectory(), "Recorder")
            if (!external.exists()) external.mkdir()

            record_view.setAudioDirectory(external)
        }else{
            askForStoragePermission()
        }

        btn_send.setOnRecordClickListener(this)
        btn_send.setRecordView(record_view)

        record_view.setOnRecordListener(object : OnAudioRecordListener {
            override fun onRecordFinished(recordingItem: RecordingItem?) {
                runOnUiThread {
//                    showToast( "Recording finished ")
                    Log.d(TAG,"Recording finished filePath : ${recordingItem?.filePath}" +
                            "\n fileLength %${recordingItem?.length}")
                    mAdapter.addItem(recordingItem)
                }
            }

            override fun onError(errorCode: Int) {
                record_view.recordTimerStop()
                if(errorCode == QTAudioRecording.FILE_NULL){
                    runOnUiThread {
                        record_view.stopRecordingnResetViews(btn_send)
                        showToast("Destination filePath is null ")
                        Log.d(TAG,"Recording error filepath is null")
                    }
                }

                Log.d(TAG,"Recording error code $errorCode")

            }

            override fun onRecordingStarted() {
                runOnUiThread {
                    Log.d(TAG,"Recording started")
                    record_view.recordTimerStart()
                }
            }
        })

        edit_message.setKeyboardListener(this)
        mAdapter = AudioFilesAdapter(mRecordingItemsList,this)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = mAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, VERTICAL_LIST))
    }



    override fun onTypingStarted() {
        if(record_view.isRecordingStarted){
            edit_message.setText("")
            return
        }
        btn_send.changeToMessage(true)
    }

    override fun onTypingStopped() {

    }

    override fun onStop() {
        super.onStop()
        if(record_view.isRecordingStarted) record_view.stopRecordingnResetViews(btn_send)
    }

    override fun onClick(v: View?) {
        Handler().postDelayed({ edit_message.setText("") }, 500)
    }

    override fun askForVoicePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), VOICE_PERMISSION_REQUEST_CODE)
    }

    private fun askForStoragePermission() {
        if(!hasExternalReadWritePermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {

                STORAGE_PERMISSION_REQUEST_CODE -> {
                    val external = File(getExternalStorageDirectory(), "RecorderM")
                    if (!external.exists()) external.mkdir()

                    record_view.setAudioDirectory(external)


                }
                else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }else{
            showToast("Please grant permission")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }

    override fun onEnterPressed(): Boolean {
        return false
    }

    override fun onTextDeleted() {
        btn_send.changeToMessage(false)
    }

    override fun textLengthChanged(length: Int) {

    }

    override fun onTabPressed(repeated: Boolean): Boolean {
        return false
    }

    override fun sendKeyboardRichContent(uri: Uri?) {
    }

    private fun hasExternalReadWritePermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    override fun onPause() {
        super.onPause()
        mAdapter.stopPlayer()
    }
}
