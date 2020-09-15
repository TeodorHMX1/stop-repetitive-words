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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zeoflow.srw.R
import java.util.*

class AdapterSequences internal constructor(private val mDataList: ArrayList<String>) : RecyclerView.Adapter<HolderSequence>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderSequence {
        return HolderSequence(LayoutInflater.from(parent.context).inflate(R.layout.item_sequence, parent, false))
    }

    override fun onBindViewHolder(holder: HolderSequence, position: Int) {
        holder.setContent(mDataList[position], position, zOnItemClicked)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    private var zOnItemClicked: OnItemClicked? = null
    fun setOnItemClicked(zOnItemClicked: OnItemClicked?) {
        this.zOnItemClicked = zOnItemClicked
    }

    interface OnItemClicked {
        fun onDeleteAction(position: Int)
    }
}