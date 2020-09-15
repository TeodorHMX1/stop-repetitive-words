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

package com.zeoflow.srw.sequences

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zeoflow.material.elements.button.MaterialButton
import com.zeoflow.material.elements.imageview.ShapeableImageView
import com.zeoflow.material.elements.textfield.TextInputEditText
import com.zeoflow.srw.R
import com.zeoflow.srw.sequences.AdapterSequences.OnItemClicked
import com.zeoflow.srw.settings.SettingsActivity
import java.util.*

class SequencesActivity : AppCompatActivity(), OnItemClicked {

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
    }

    override fun onBackPressed() {
        val intent = Intent(this@SequencesActivity, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }

    val mListSequences = ArrayList<String>()
    val mAdapter = AdapterSequences(mListSequences)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequences)

        val zBack = findViewById<ShapeableImageView?>(R.id.simBack)
        val btManageSequences = findViewById<MaterialButton?>(R.id.btManageSequences)
        val edtSequence = findViewById<TextInputEditText?>(R.id.edtSequence)
        val rvSequences = findViewById<RecyclerView?>(R.id.rvSequences)

        zBack!!.setOnClickListener { view: View? ->
            val intent = Intent(this@SequencesActivity, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        btManageSequences!!.setOnClickListener { view: View? ->
            if(Objects.requireNonNull(edtSequence!!.text).toString().isNotEmpty()) {
                mListSequences.add(0, Objects.requireNonNull(edtSequence.text).toString())
                mAdapter.notifyItemInserted(0)
                edtSequence.setText("")
                val set: Set<String> = HashSet(mListSequences)
                val scoreEditor: SharedPreferences.Editor = getSharedPreferences("ZFLOW_SP_APP_DATA", Context.MODE_PRIVATE).edit()
                scoreEditor.putStringSet("key", set)
                scoreEditor.apply()
            }
        }

        rvSequences!!.itemAnimator = DefaultItemAnimator()
        rvSequences.layoutManager = LinearLayoutManager(this)
        rvSequences.setHasFixedSize(false)
        rvSequences.adapter = mAdapter

        mAdapter.setOnItemClicked(this@SequencesActivity)

        val spList: SharedPreferences = getSharedPreferences("ZFLOW_SP_APP_DATA", Context.MODE_PRIVATE)
        val set: Set<String> = spList.getStringSet("key", HashSet(mListSequences)) as Set<String>
        mListSequences.addAll(set)
        mAdapter.notifyDataSetChanged()
    }

    override fun onDeleteAction(position: Int) {
        mListSequences.removeAt(position)
        mAdapter.notifyDataSetChanged()

        val set: Set<String> = HashSet(mListSequences)
        val scoreEditor: SharedPreferences.Editor = getSharedPreferences("ZFLOW_SP_APP_DATA", Context.MODE_PRIVATE).edit()
        scoreEditor.putStringSet("key", set)
        scoreEditor.apply()
    }

}