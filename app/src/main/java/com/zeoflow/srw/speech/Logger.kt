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

import com.zeoflow.srw.BuildConfig

class Logger private constructor() {
    enum class LogLevel {
        DEBUG, INFO, ERROR, OFF
    }

    interface LoggerDelegate {
        fun error(tag: String?, message: String?)
        fun error(tag: String?, message: String?, exception: Throwable?)
        fun debug(tag: String?, message: String?)
        fun info(tag: String?, message: String?)
    }

    private var mLogLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.OFF
    private var mDelegate: LoggerDelegate = DefaultLoggerDelegate()

    private object SingletonHolder {
        val instance = Logger()
    }

    companion object {
        fun resetLoggerDelegate() {
            synchronized(Logger::class.java) { SingletonHolder.instance.mDelegate = DefaultLoggerDelegate() }
        }

        fun setLoggerDelegate(delegate: LoggerDelegate?) {
            requireNotNull(delegate) { "delegate MUST not be null!" }
            synchronized(Logger::class.java) { SingletonHolder.instance.mDelegate = delegate }
        }

        fun setLogLevel(level: LogLevel) {
            synchronized(Logger::class.java) { SingletonHolder.instance.mLogLevel = level }
        }

        fun error(tag: String?, message: String?) {
            if (SingletonHolder.instance.mLogLevel <= LogLevel.ERROR) {
                SingletonHolder.instance.mDelegate.error(tag, message)
            }
        }

        fun error(tag: String?, message: String?, exception: Throwable?) {
            if (SingletonHolder.instance.mLogLevel <= LogLevel.ERROR) {
                SingletonHolder.instance.mDelegate.error(tag, message, exception)
            }
        }

        fun info(tag: String?, message: String?) {
            if (SingletonHolder.instance.mLogLevel <= LogLevel.INFO) {
                SingletonHolder.instance.mDelegate.info(tag, message)
            }
        }

        @JvmStatic
        fun debug(tag: String?, message: String?) {
            if (SingletonHolder.instance.mLogLevel <= LogLevel.DEBUG) {
                SingletonHolder.instance.mDelegate.debug(tag, message)
            }
        }
    }
}