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

import android.content.Context
import android.os.Handler
import android.speech.tts.UtteranceProgressListener
import java.lang.ref.WeakReference

class TtsProgressListener(context: Context, mTtsCallbacks: MutableMap<String, TextToSpeechCallback>) : UtteranceProgressListener() {
    private val mTtsCallbacks: MutableMap<String, TextToSpeechCallback>
    private val contextWeakReference: WeakReference<Context>
    override fun onStart(utteranceId: String) {
        val callback = mTtsCallbacks[utteranceId]
        val context = contextWeakReference.get()
        if (callback != null && context != null) {
            Handler(context.mainLooper).post { callback.onStart() }
        }
    }

    override fun onDone(utteranceId: String) {
        val callback = mTtsCallbacks[utteranceId]
        val context = contextWeakReference.get()
        if (callback != null && context != null) {
            Handler(context.mainLooper).post {
                callback.onCompleted()
                mTtsCallbacks.remove(utteranceId)
            }
        }
    }

    override fun onError(utteranceId: String) {
        val callback = mTtsCallbacks[utteranceId]
        val context = contextWeakReference.get()
        if (callback != null && context != null) {
            Handler(context.mainLooper).post {
                callback.onError()
                mTtsCallbacks.remove(utteranceId)
            }
        }
    }

    init {
        contextWeakReference = WeakReference(context)
        this.mTtsCallbacks = mTtsCallbacks
    }
}