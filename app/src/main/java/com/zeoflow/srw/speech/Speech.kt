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

package com.zeoflow.srw.speech

import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import com.zeoflow.srw.speech.Speech
import com.zeoflow.srw.speech.ui.SpeechProgressView

/**
 * Helper class to easily work with Android speech recognition.
 *
 * @author Sachin Varma
 */
class Speech {
    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mProgressView: SpeechProgressView? = null
    private var mCallingPackage: String? = null
    private var mPreferOffline: Boolean = false
    private var mGetPartialResults: Boolean = true
    private var mDelegate: SpeechDelegate? = null

    /**
     * Check if voice recognition is currently active.
     *
     * @return true if the voice recognition is on, false otherwise
     */
    var isListening: Boolean = false
        private set
    private val mPartialData: MutableList<String> = java.util.ArrayList<String>()
    private var mUnstableData: String? = null
    private var mDelayedStopListening: DelayedOperation? = null
    private var mContext: android.content.Context? = null
    private var mTextToSpeech: TextToSpeech? = null
    private val mTtsCallbacks: MutableMap<String, TextToSpeechCallback> = java.util.HashMap<String, TextToSpeechCallback>()
    private var mLocale: java.util.Locale = java.util.Locale.getDefault()
    private var mTtsRate: Float = 1.0f
    private var mTtsPitch: Float = 1.0f
    private var mTtsQueueMode: Int = TextToSpeech.QUEUE_FLUSH
    private var mStopListeningDelayInMs: Long = 10000
    private var mTransitionMinimumDelay: Long = 1200
    private var mLastActionTimestamp: Long = 0
    private var mLastPartialResults: List<String>? = null
    private val mTttsInitListener: OnInitListener = OnInitListener { status ->
        when (status) {
            TextToSpeech.SUCCESS -> Logger.info(LOG_TAG, "TextToSpeech engine successfully started")
            TextToSpeech.ERROR -> Logger.error(LOG_TAG, "Error while initializing TextToSpeech engine!")
            else -> Logger.error(LOG_TAG, "Unknown TextToSpeech status: " + status)
        }
    }
    private var mTtsProgressListener: UtteranceProgressListener? = null
    private var mAudioManager: AudioManager? = null
    private var mStreamVolume: Int = 0
    private val mListener: RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(bundle: Bundle) {
            mPartialData.clear()
            mUnstableData = null
            mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0) // again setting the system volume back to the original, un-mutting
        }

        override fun onBeginningOfSpeech() {
            if (mProgressView != null) mProgressView!!.onBeginningOfSpeech()
            mDelayedStopListening!!.start(object : DelayedOperation.Operation {
                override fun onDelayedOperation() {
                    returnPartialResultsAndRecreateSpeechRecognizer()
                    android.util.Log.d("ReachedStop", "Stoppong")
                    //  mListenerDelay.onClick("1");
                }

                override fun shouldExecuteDelayedOperation(): Boolean {
                    return true
                }
            })
        }

        override fun onRmsChanged(v: Float) {
            try {
                if (mDelegate != null) mDelegate!!.onSpeechRmsChanged(v)
            } catch (exc: Throwable) {
                Logger.error(Speech::class.java.simpleName,
                        "Unhandled exception in delegate onSpeechRmsChanged", exc)
            }
            if (mProgressView != null) mProgressView!!.onRmsChanged(v)
        }

        override fun onPartialResults(bundle: Bundle) {
            mDelayedStopListening!!.resetTimer()
            val partialResults: List<String>? = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val unstableData: List<String>? = bundle.getStringArrayList("android.speech.extra.UNSTABLE_TEXT")
            if (partialResults != null && partialResults.isNotEmpty()) {
                mPartialData.clear()
                mPartialData.addAll(partialResults)
                mUnstableData = if (unstableData != null && unstableData.isNotEmpty()) unstableData[0] else null
                try {
                    if (mLastPartialResults == null || !(mLastPartialResults == partialResults)) {
                        if (mDelegate != null) mDelegate!!.onSpeechPartialResults(partialResults)
                        mLastPartialResults = partialResults
                    }
                } catch (exc: Throwable) {
                    Logger.error(Speech::class.java.simpleName,
                            "Unhandled exception in delegate onSpeechPartialResults", exc)
                }
            }
        }

        override fun onResults(bundle: Bundle) {
            mDelayedStopListening!!.cancel()
            val results: List<String?>? = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val result: String?
            result = if (((results != null) && results.isNotEmpty()
                            && (results[0] != null) && results[0]!!.isNotEmpty())) {
                results[0]
            } else {
                Logger.info(Speech::class.java.simpleName, "No speech results, getting partial")
                partialResultsAsString
            }
            isListening = false
            try {
                if (mDelegate != null) mDelegate!!.onSpeechResult(result!!.trim { it <= ' ' })
            } catch (exc: Throwable) {
                Logger.error(Speech::class.java.simpleName,
                        "Unhandled exception in delegate onSpeechResult", exc)
            }
            if (mProgressView != null) mProgressView!!.onResultOrOnError()
            initSpeechRecognizer(mContext)
        }

        override fun onError(code: Int) {
            returnPartialResultsAndRecreateSpeechRecognizer()
        }

        override fun onBufferReceived(bytes: ByteArray) {}
        override fun onEndOfSpeech() {
            if (mProgressView != null) mProgressView!!.onEndOfSpeech()
        }

        override fun onEvent(i: Int, bundle: Bundle) {}
    }

    private constructor(context: android.content.Context) {
        initSpeechRecognizer(context)
        initTts(context)
    }

    private constructor(context: android.content.Context, callingPackage: String) {
        initSpeechRecognizer(context)
        initTts(context)
        mCallingPackage = callingPackage
    }

    private fun initSpeechRecognizer(context: android.content.Context?) {
        if (context == null) throw java.lang.IllegalArgumentException("context must be defined!")
        mContext = context
        mAudioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager?
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            if (mSpeechRecognizer != null) {
                try {
                    mSpeechRecognizer!!.destroy()
                } catch (exc: Throwable) {
                    Logger.debug(Speech::class.java.getSimpleName(),
                            "Non-Fatal error while destroying speech. " + exc.message)
                } finally {
                    mSpeechRecognizer = null
                }
            }
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            with(mSpeechRecognizer) { this?.setRecognitionListener(mListener) }
            initDelayedStopListening(context)
        } else {
            mSpeechRecognizer = null
        }
        mPartialData.clear()
        mUnstableData = null
    }

    private fun initTts(context: android.content.Context) {
        if (mTextToSpeech == null) {
            mTtsProgressListener = mContext?.let { TtsProgressListener(it, mTtsCallbacks) }
            mTextToSpeech = TextToSpeech(context.applicationContext, mTttsInitListener)
            mTextToSpeech!!.setOnUtteranceProgressListener(mTtsProgressListener)
            mTextToSpeech!!.language = mLocale
            mTextToSpeech!!.setPitch(mTtsPitch)
            mTextToSpeech!!.setSpeechRate(mTtsRate)
        }
    }

    private fun initDelayedStopListening(context: android.content.Context?) {
        if (mDelayedStopListening != null) {
            mDelayedStopListening!!.cancel()
            mDelayedStopListening = null
        }
        //        Toast.makeText(context, "destroyed", Toast.LENGTH_SHORT).show();
        mListenerDelay?.onSpecifiedCommandPronounced("1")
        mDelayedStopListening = DelayedOperation(context, "delayStopListening", mStopListeningDelayInMs)
    }

    /**
     * Must be called inside Activity's onDestroy.
     */
    @Synchronized
    fun shutdown() {
        if (mSpeechRecognizer != null) {
            try {
                mSpeechRecognizer!!.stopListening()
            } catch (exc: java.lang.Exception) {
                Logger.error(javaClass.simpleName, "Warning while de-initing speech recognizer", exc)
            }
        }
        if (mTextToSpeech != null) {
            try {
                mTtsCallbacks.clear()
                mTextToSpeech!!.stop()
                mTextToSpeech!!.shutdown()
            } catch (exc: java.lang.Exception) {
                Logger.error(javaClass.simpleName, "Warning while de-initing text to speech", exc)
            }
        }
        unregisterDelegate()
        instance = null
    }

    /**
     * Starts voice recognition.
     *
     * @param delegate delegate which will receive speech recognition events and status
     * @throws SpeechRecognitionNotAvailable      when speech recognition is not available on the device
     * @throws GoogleVoiceTypingDisabledException when google voice typing is disabled on the device
     */
    @Throws(SpeechRecognitionNotAvailable::class, GoogleVoiceTypingDisabledException::class)
    fun startListening(delegate: SpeechDelegate?) {
        startListening(null, delegate)
    }

    /**
     * Starts voice recognition.
     *
     * @param progressView view in which to draw speech animation
     * @param delegate     delegate which will receive speech recognition events and status
     * @throws SpeechRecognitionNotAvailable      when speech recognition is not available on the device
     * @throws GoogleVoiceTypingDisabledException when google voice typing is disabled on the device
     */
    @Throws(SpeechRecognitionNotAvailable::class, GoogleVoiceTypingDisabledException::class)
    fun startListening(progressView: SpeechProgressView?, delegate: SpeechDelegate?) {
        if (this.isListening) return
        if (mSpeechRecognizer == null) throw SpeechRecognitionNotAvailable()
        if (delegate == null) throw java.lang.IllegalArgumentException("delegate must be defined!")
        if (throttleAction()) {
            Logger.debug(javaClass.simpleName, "Hey man calm down! Throttling start to prevent disaster!")
            return
        }
        mStreamVolume = (mAudioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
                ?:  // getting system volume into var for later un-muting
                mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)) as Int // setting system volume to zero, muting
        //
//        if (progressView != null && !(progressView.getParent() instanceof LinearLayout))
//            throw new IllegalArgumentException("progressView must be put inside a LinearLayout!");
//
//        mProgressView = progressView;
        mDelegate = delegate
        val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, mGetPartialResults)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ro-RO")
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        if (mCallingPackage != null && mCallingPackage!!.isNotEmpty()) {
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mCallingPackage)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, mPreferOffline)
        }
        try {
            mSpeechRecognizer!!.startListening(intent)
        } catch (exc: java.lang.SecurityException) {
            throw GoogleVoiceTypingDisabledException()
        }
        this.isListening = true
        updateLastActionTimestamp()
        try {
            mDelegate?.onStartOfSpeech()
        } catch (exc: Throwable) {
            Logger.error(Speech::class.java.getSimpleName(),
                    "Unhandled exception in delegate onStartOfSpeech", exc)
        }
    }

    private fun unregisterDelegate() {
        mDelegate = null
        mProgressView = null
    }

    private fun updateLastActionTimestamp() {
        mLastActionTimestamp = java.util.Date().getTime()
    }

    private fun throttleAction(): Boolean {
        return (java.util.Date().getTime() <= (mLastActionTimestamp + mTransitionMinimumDelay))
    }

    /**
     * Stops voice recognition listening.
     * This method does nothing if voice listening is not active
     */
    fun stopListening() {
        if (!this.isListening) return
        if (throttleAction()) {
            Logger.debug(javaClass.getSimpleName(), "Hey man calm down! Throttling stop to prevent disaster!")
            return
        }
        this.isListening = false
        updateLastActionTimestamp()
        returnPartialResultsAndRecreateSpeechRecognizer()
    }

    private val partialResultsAsString: String
        private get() {
            val out: java.lang.StringBuilder = java.lang.StringBuilder("")
            for (partial: String in mPartialData) {
                out.append(partial).append(" ")
            }
            if (mUnstableData != null && !mUnstableData!!.isEmpty()) out.append(mUnstableData)
            return out.toString().trim({ it <= ' ' })
        }

    private fun returnPartialResultsAndRecreateSpeechRecognizer() {
        this.isListening = false
        try {
            mDelegate?.onSpeechResult(partialResultsAsString)
        } catch (exc: Throwable) {
            Logger.error(Speech::class.java.getSimpleName(),
                    "Unhandled exception in delegate onSpeechResult", exc)
        }

//        if (mProgressView != null)
//            mProgressView.onResultOrOnError();

        // recreate the speech recognizer
        initSpeechRecognizer(mContext)
    }
    /**
     * Uses text to speech to transform a written message into a sound.
     *
     * @param message  message to play
     * @param callback callback which will receive progress status of the operation
     */
    /**
     * Uses text to speech to transform a written message into a sound.
     *
     * @param message message to play
     */
    @JvmOverloads
    fun say(message: String?, callback: TextToSpeechCallback? = null) {
        val utteranceId: String = java.util.UUID.randomUUID().toString()
        if (callback != null) {
            mTtsCallbacks[utteranceId] = callback
        }
        mTextToSpeech?.speak(message, mTtsQueueMode, null, utteranceId)
    }

    /**
     * Stops text to speech.
     */
    fun stopTextToSpeech() {
        mTextToSpeech?.stop()
    }

    /**
     * Set whether to only use an offline speech recognition engine.
     * The default is false, meaning that either network or offline recognition engines may be used.
     *
     * @param preferOffline true to prefer offline engine, false to use either one of the two
     * @return speech instance
     */
    fun setPreferOffline(preferOffline: Boolean): Speech {
        mPreferOffline = preferOffline
        return this
    }

    /**
     * Set whether partial results should be returned by the recognizer as the user speaks
     * (default is true). The server may ignore a request for partial results in some or all cases.
     *
     * @param getPartialResults true to get also partial recognition results, false otherwise
     * @return speech instance
     */
    fun setGetPartialResults(getPartialResults: Boolean): Speech {
        mGetPartialResults = getPartialResults
        return this
    }

    /**
     * Sets text to speech and recognition language.
     * Defaults to device language setting.
     *
     * @param locale new locale
     * @return speech instance
     */
    fun setLocale(locale: java.util.Locale): Speech {
        mLocale = locale
        mTextToSpeech?.setLanguage(locale)
        return this
    }

    /**
     * Sets the speech rate. This has no effect on any pre-recorded speech.
     *
     * @param rate Speech rate. 1.0 is the normal speech rate, lower values slow down the speech
     * (0.5 is half the normal speech rate), greater values accelerate it
     * (2.0 is twice the normal speech rate).
     * @return speech instance
     */
    fun setTextToSpeechRate(rate: Float): Speech {
        mTtsRate = rate
        mTextToSpeech?.setSpeechRate(rate)
        return this
    }

    /**
     * Sets the speech pitch for the TextToSpeech engine.
     * This has no effect on any pre-recorded speech.
     *
     * @param pitch Speech pitch. 1.0 is the normal pitch, lower values lower the tone of the
     * synthesized voice, greater values increase it.
     * @return speech instance
     */
    fun setTextToSpeechPitch(pitch: Float): Speech {
        mTtsPitch = pitch
        mTextToSpeech?.setPitch(pitch)
        return this
    }

    /**
     * Sets the idle timeout after which the listening will be automatically stopped.
     *
     * @param milliseconds timeout in milliseconds
     * @return speech instance
     */
    fun setStopListeningAfterInactivity(milliseconds: Long): Speech {
        mStopListeningDelayInMs = milliseconds
        initDelayedStopListening(mContext)
        return this
    }

    /**
     * Sets the minimum interval between start/stop events. This is useful to prevent
     * monkey input from users.
     *
     * @param milliseconds minimum interval betweeb state change in milliseconds
     * @return speech instance
     */
    fun setTransitionMinimumDelay(milliseconds: Long): Speech {
        mTransitionMinimumDelay = milliseconds
        return this
    }

    /**
     * Sets the text to speech queue mode.
     * By default is TextToSpeech.QUEUE_FLUSH, which is faster, because it clears all the
     * messages before speaking the new one. TextToSpeech.QUEUE_ADD adds the last message
     * to speak in the queue, without clearing the messages that have been added.
     *
     * @param mode It can be either TextToSpeech.QUEUE_ADD or TextToSpeech.QUEUE_FLUSH.
     * @return speech instance
     */
    fun setTextToSpeechQueueMode(mode: Int): Speech {
        mTtsQueueMode = mode
        return this
    }

    private var mListenerDelay: stopDueToDelay? = null

    // define listener
    interface stopDueToDelay {
        fun onSpecifiedCommandPronounced(event: String?)
    }

    // set the listener. Must be called from the fragment
    fun setListener(listener: stopDueToDelay?) {
        this.mListenerDelay = listener
    }

    companion object {
        private val LOG_TAG: String = Speech::class.java.getSimpleName()
        private var instance: Speech? = null

        /**
         * Initializes speech recognition.
         *
         * @param context application context
         * @return speech instance
         */
        fun init(context: android.content.Context): Speech? {
            if (instance == null) {
                instance = Speech(context)
            }
            return instance
        }

        /**
         * Initializes speech recognition.
         *
         * @param context        application context
         * @param callingPackage The extra key used in an intent to the speech recognizer for
         * voice search. Not generally to be used by developers.
         * The system search dialog uses this, for example, to set a calling
         * package for identification by a voice search API.
         * If this extra is set by anyone but the system process,
         * it should be overridden by the voice search implementation.
         * By passing null or empty string (which is the default) you are
         * not overriding the calling package
         * @return speech instance
         */
        fun init(context: android.content.Context, callingPackage: String): Speech? {
            if (instance == null) {
                instance = Speech(context, callingPackage)
            }
            return instance
        }

        /**
         * Gets speech recognition instance.
         *
         * @return SpeechRecognition instance
         */
        fun getInstance(): Speech? {
            if (instance == null) {
                throw java.lang.IllegalStateException("Speech recognition has not been initialized! call init method first!")
            }
            return instance
        }

        fun isRunning(): Boolean {
            return instance != null
        }
    }
}