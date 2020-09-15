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

import android.util.Log
import com.zeoflow.srw.speech.Logger.LoggerDelegate
import com.zeoflow.srw.speech.Speech

class DefaultLoggerDelegate : LoggerDelegate {
    override fun error(tag: String?, message: String?) {
        Log.e(TAG, "$tag - $message")
    }

    override fun error(tag: String?, message: String?, exception: Throwable?) {
        Log.e(TAG, "$tag - $message", exception)
    }

    override fun debug(tag: String?, message: String?) {
        Log.d(TAG, "$tag - $message")
    }

    override fun info(tag: String?, message: String?) {
        Log.i(TAG, "$tag - $message")
    }

    companion object {
        private val TAG = Speech::class.java.simpleName
    }
}