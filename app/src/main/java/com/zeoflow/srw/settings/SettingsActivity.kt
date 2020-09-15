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

package com.zeoflow.srw.settings

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zeoflow.material.elements.button.MaterialButton
import com.zeoflow.material.elements.imageview.ShapeableImageView
import com.zeoflow.material.elements.switchmaterial.SwitchMaterial
import com.zeoflow.srw.R
import com.zeoflow.srw.foreground.service.DetectionService
import com.zeoflow.srw.home.MainActivity
import com.zeoflow.srw.sequences.SequencesActivity
import com.zeoflow.srw.utils.Constants

class SettingsActivity : AppCompatActivity() {
    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
    }

    override fun onBackPressed() {
        val intent = Intent(this@SettingsActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val zBack = findViewById<ShapeableImageView>(R.id.simBack)
        zBack.setOnClickListener { view: View? ->
            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val swStartService = findViewById<SwitchMaterial>(R.id.swStartService)
        swStartService.isChecked = isMyServiceRunning
        swStartService.setOnClickListener { v: View? ->
            if (!isMyServiceRunning) {
                val startIntent = Intent(this@SettingsActivity, DetectionService::class.java)
                startIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
                startService(startIntent)
                swStartService.isChecked = true
            } else {
                val stopIntent = Intent(this@SettingsActivity, DetectionService::class.java)
                stopIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
                startService(stopIntent)
                swStartService.isChecked = false
            }
        }
        val btManageSequences = findViewById<MaterialButton>(R.id.btManageSequences)
        btManageSequences.setOnClickListener { view: View? ->
            val intent = Intent(this@SettingsActivity, SequencesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private val isMyServiceRunning: Boolean
        get() {
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (DetectionService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }
}