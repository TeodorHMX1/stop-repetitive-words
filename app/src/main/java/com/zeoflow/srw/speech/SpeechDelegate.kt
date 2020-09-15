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

interface SpeechDelegate {
    /**
     * Invoked when the speech recognition is started.
     */
    fun onStartOfSpeech()

    /**
     * The sound level in the audio stream has changed.
     * There is no guarantee that this method will be called.
     * @param value the new RMS dB value
     */
    fun onSpeechRmsChanged(value: Float)

    /**
     * Invoked when there are partial speech results.
     * @param results list of strings. This is ensured to be non null and non empty.
     */
    fun onSpeechPartialResults(results: List<String?>?)

    /**
     * Invoked when there is a speech result
     * @param result string resulting from speech recognition.
     * This is ensured to be non null.
     */
    fun onSpeechResult(result: String?)
}