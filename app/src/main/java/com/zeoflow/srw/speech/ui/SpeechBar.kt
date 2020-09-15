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

package com.zeoflow.srw.speech.ui

import android.graphics.RectF

class SpeechBar(var x: Int, var y: Int, height: Int, maxHeight: Int, val radius: Int) {
    var height: Int
    val maxHeight: Int
    val startX: Int
    val startY: Int
    val rect: RectF
    fun update() {
        rect[x - radius.toFloat(), y - height / 2.toFloat(), x + radius.toFloat()] = y + height / 2.toFloat()
    }

    init {
        startX = x
        startY = y
        this.height = height
        this.maxHeight = maxHeight
        rect = RectF((x - radius).toFloat(),
                (y - height / 2).toFloat(),
                (x + radius).toFloat(),
                (y + height / 2).toFloat())
    }
}