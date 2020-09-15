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

package com.zeoflow.srw.utils

class Constants {
    interface ACTION {
        companion object {
            const val MAIN_ACTION = "com.zeoflow.foregroundservice.action.main"
            const val STOP_ACTION = "com.zeoflow.foregroundservice.action.stop"
            const val SETTINGS_ACTION = "com.zeoflow.foregroundservice.action.settings"
            const val STARTFOREGROUND_ACTION = "com.zeoflow.foregroundservice.action.startforeground"
            const val STOPFOREGROUND_ACTION = "com.zeoflow.foregroundservice.action.stopforeground"
        }
    }

    interface NOTIFICATIONS_ID {
        companion object {
            const val WORDS_RECOGNITION_ON = 752715
            const val ENABLE_WORDS_RECOGNITION = 463455
        }
    }
}