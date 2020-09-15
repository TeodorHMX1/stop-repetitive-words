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
import com.zeoflow.srw.speech.Logger.Companion.debug
import java.util.*

class DelayedOperation(context: Context?, tag: String, delayInMilliseconds: Long) {
    interface Operation {
        fun onDelayedOperation()
        fun shouldExecuteDelayedOperation(): Boolean
    }

    private val mDelay: Long
    private var mOperation: Operation? = null
    private var mTimer: Timer? = null
    private var started = false
    private val mContext: Context
    private val mTag: String
    fun start(operation: Operation?) {
        requireNotNull(operation) { "The operation must be defined!" }
        debug(LOG_TAG, "starting delayed operation with tag: $mTag")
        mOperation = operation
        cancel()
        started = true
        resetTimer()
    }

    fun resetTimer() {
        if (!started) return
        if (mTimer != null) mTimer!!.cancel()
        debug(LOG_TAG, "resetting delayed operation with tag: $mTag")
        mTimer = Timer()
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                if (mOperation!!.shouldExecuteDelayedOperation()) {
                    debug(LOG_TAG, "executing delayed operation with tag: $mTag")
                    Handler(mContext.mainLooper).post { mOperation!!.onDelayedOperation() }
                }
                cancel()
            }
        }, mDelay)
    }

    fun cancel() {
        if (mTimer != null) {
            debug(LOG_TAG, "cancelled delayed operation with tag: $mTag")
            mTimer!!.cancel()
            mTimer = null
        }
        started = false
    }

    companion object {
        private val LOG_TAG = DelayedOperation::class.java.simpleName
    }

    init {
        requireNotNull(context) { "Context is null" }
        require(delayInMilliseconds > 0) { "The delay in milliseconds must be > 0" }
        mContext = context
        mTag = tag
        mDelay = delayInMilliseconds
        debug(LOG_TAG, "created delayed operation with tag: $mTag")
    }
}