/*
 * Copyright (c) 2020. Teodor G. (https://www.github.com/TeodorHMX1).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.zeoflow.srw.foreground.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zeoflow.srw.R
import com.zeoflow.srw.home.MainActivity
import com.zeoflow.srw.settings.SettingsActivity
import com.zeoflow.srw.speech.GoogleVoiceTypingDisabledException
import com.zeoflow.srw.speech.Speech
import com.zeoflow.srw.speech.Speech.Companion.isRunning
import com.zeoflow.srw.speech.Speech.stopDueToDelay
import com.zeoflow.srw.speech.SpeechDelegate
import com.zeoflow.srw.speech.SpeechRecognitionNotAvailable
import com.zeoflow.srw.utils.Constants
import java.util.*

class DetectionService : Service(), SpeechDelegate, stopDueToDelay {
    var delegate: SpeechDelegate? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (Objects.requireNonNull(intent.action)) {
            Constants.ACTION.STARTFOREGROUND_ACTION -> startVoiceService()
            Constants.ACTION.STOP_ACTION -> stopVoiceService()
            Constants.ACTION.SETTINGS_ACTION -> openSettings()
            Constants.ACTION.STOPFOREGROUND_ACTION -> stopVoiceService()
        }
        return START_STICKY
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun stopVoiceService() {
        if (isRunning()) {
            Speech.getInstance()?.shutdown()
        }
        stopForeground(true)
        stopSelf()
        sendOffNotification()
    }

    private fun sendOffNotification() {
        (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
        (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
        (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0)
        (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
        (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = Constants.ACTION.MAIN_ACTION
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)
        val previousIntent = Intent(this, DetectionService::class.java)
        previousIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
        val ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0)
        val playIntent = Intent(this, DetectionService::class.java)
        playIntent.action = Constants.ACTION.SETTINGS_ACTION
        val pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0)
        val CHANNEL_ID = "enable-words-recognition"
        createNotificationChannel(CHANNEL_ID)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Words-Recognition is off")
                .setContentText("Turn it on to enable the recognition in the background")
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("Turn it on to enable the recognition in the background"))
                .setSmallIcon(R.drawable.ic_notify)
                .setContentIntent(pendingIntent)
                .setOngoing(false)
                .setAutoCancel(false)
                .setColor(ContextCompat.getColor(this, R.color.coloredAccent))
                .addAction(android.R.drawable.ic_media_previous, "Turn on", ppreviousIntent)
                .addAction(android.R.drawable.ic_media_previous, "Settings", pplayIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(Constants.NOTIFICATIONS_ID.ENABLE_WORDS_RECOGNITION, notification)
    }

    private fun startVoiceService() {
        sendNotification()
        initializeVoiceRecognizer()
    }

    private fun sendNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = Constants.ACTION.MAIN_ACTION
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)
        val previousIntent = Intent(this, DetectionService::class.java)
        previousIntent.action = Constants.ACTION.STOP_ACTION
        val ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0)
        val playIntent = Intent(this, DetectionService::class.java)
        playIntent.action = Constants.ACTION.SETTINGS_ACTION
        val pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0)
        val CHANNEL_ID = "words-recognition"
        createNotificationChannel(CHANNEL_ID)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Words-Recognition is on")
                .setContentText("You can talk freely while the words settled by you will be flagged in the background")
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("You can talk freely while the words settled by you will be flagged in the background"))
                .setSmallIcon(R.drawable.ic_notify)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setColor(ContextCompat.getColor(this, R.color.coloredAccent))
                .addAction(android.R.drawable.ic_media_previous, "Turn off", ppreviousIntent)
                .addAction(android.R.drawable.ic_media_previous, "Settings", pplayIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(Constants.NOTIFICATIONS_ID.ENABLE_WORDS_RECOGNITION)
        startForeground(Constants.NOTIFICATIONS_ID.WORDS_RECOGNITION_ON, notification)
    }

    private fun createNotificationChannel(CHANNEL_ID: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_ID,
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun initializeVoiceRecognizer() {
        try {
            (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
            (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0)
            (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
            (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Speech.init(this)
        delegate = this
        Speech.getInstance()?.setListener(this)
        if (Speech.getInstance()?.isListening!!) {
            Speech.getInstance()?.stopListening()
        } else {
            try {
                Speech.getInstance()?.stopTextToSpeech()
                Speech.getInstance()?.startListening(null, this)
            } catch (exc: SpeechRecognitionNotAvailable) {
                //showSpeechNotSupportedDialog();
            } catch (exc: GoogleVoiceTypingDisabledException) {
                //showEnableGoogleVoiceTyping();
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartOfSpeech() {}
    override fun onSpeechRmsChanged(value: Float) {}
    override fun onSpeechPartialResults(results: List<String?>?) {
        for (partial in results!!) {
            val sp = getSharedPreferences("ZFLOW_SP_APP_DATA", MODE_PRIVATE)
            val mListSequences = ArrayList<String>()
            val set = sp.getStringSet("key", HashSet(mListSequences))!!
            mListSequences.addAll(set)
            for (i in mListSequences.indices) {
                if (results.contains(mListSequences[i])) {
                    val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        v.vibrate(500)
                    }
                }
            }
        }
    }

    override fun onSpeechResult(result: String?) {
        if (!TextUtils.isEmpty(result)) {
            val sp = getSharedPreferences("ZFLOW_SP_APP_DATA", MODE_PRIVATE)
            val mListSequences = ArrayList<String>()
            val set = sp.getStringSet("key", HashSet(mListSequences))!!
            mListSequences.addAll(set)
            for (i in mListSequences.indices) {
                if (result?.contains(mListSequences[i])!!) {
                    val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        v.vibrate(500)
                    }
                }
            }
        }
    }

    override fun onSpecifiedCommandPronounced(event: String?) {
        try {
            (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
            (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0)
            (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
            (Objects.requireNonNull(getSystemService(AUDIO_SERVICE)) as AudioManager).adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isRunning()) {
            if (Speech.getInstance()?.isListening!!) {
                Speech.getInstance()?.stopListening()
            } else {
                try {
                    Speech.getInstance()?.stopTextToSpeech()
                    Speech.getInstance()?.startListening(null, this)
                } catch (exc: SpeechRecognitionNotAvailable) {
                    //showSpeechNotSupportedDialog();
                } catch (exc: GoogleVoiceTypingDisabledException) {
                    //showEnableGoogleVoiceTyping();
                }
            }
        }
    }
}