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

import android.speech.SpeechRecognizer

class SpeechRecognitionException(val code: Int) : Exception(getMessage(code)) {

    companion object {
        private fun getMessage(code: Int): String {
            val message: String
            message = when (code) {
                SpeechRecognizer.ERROR_AUDIO -> "$code - Audio recording error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "$code - Insufficient permissions. Request android.permission.RECORD_AUDIO"
                SpeechRecognizer.ERROR_CLIENT ->                 // http://stackoverflow.com/questions/24995565/android-speechrecognizer-when-do-i-get-error-client-when-starting-the-voice-reco
                    "$code - Client side error. Maybe your internet connection is poor!"
                SpeechRecognizer.ERROR_NETWORK -> "$code - Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "$code - Network operation timed out"
                SpeechRecognizer.ERROR_NO_MATCH -> "$code - No recognition result matched. Try turning on partial results as a workaround."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "$code - RecognitionService busy"
                SpeechRecognizer.ERROR_SERVER -> "$code - Server sends error status"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "$code - No speech input"
                else -> "$code - Unknown exception"
            }
            return message
        }
    }
}