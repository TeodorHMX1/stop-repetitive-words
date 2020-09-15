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

package com.zeoflow.srw.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.zeoflow.srw.home.MainActivity
import com.zeoflow.srw.utils.Constants

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationManager = NotificationManagerCompat.from(this)
        val intent = Intent(this, MainActivity::class.java)

        notificationManager.cancel(Constants.NOTIFICATIONS_ID.ENABLE_WORDS_RECOGNITION)
        startActivity(intent)
        finish()
    }

}