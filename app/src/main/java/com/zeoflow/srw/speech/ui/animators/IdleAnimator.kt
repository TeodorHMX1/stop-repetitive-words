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

package com.zeoflow.srw.speech.ui.animators

import com.zeoflow.srw.speech.ui.SpeechBar

class IdleAnimator(private val bars: List<SpeechBar>, private val floatingAmplitude: Int) : BarParamsAnimator {
    private var startTimestamp: Long = 0
    private var isPlaying = false
    override fun start() {
        isPlaying = true
        startTimestamp = System.currentTimeMillis()
    }

    override fun stop() {
        isPlaying = false
    }

    override fun animate() {
        if (isPlaying) {
            update(bars)
        }
    }

    fun update(bars: List<SpeechBar>) {
        val currTimestamp = System.currentTimeMillis()
        if (currTimestamp - startTimestamp > IDLE_DURATION) {
            startTimestamp += IDLE_DURATION
        }
        val delta = currTimestamp - startTimestamp
        var i = 0
        for (bar in bars) {
            updateCirclePosition(bar, delta, i)
            i++
        }
    }

    private fun updateCirclePosition(bar: SpeechBar, delta: Long, num: Int) {
        val angle = delta.toFloat() / IDLE_DURATION * 360f + 120f * num
        val y = (Math.sin(Math.toRadians(angle.toDouble())) * floatingAmplitude).toInt() + bar.startY
        bar.y = y
        bar.update()
    }

    companion object {
        private const val IDLE_DURATION: Long = 1500
    }
}