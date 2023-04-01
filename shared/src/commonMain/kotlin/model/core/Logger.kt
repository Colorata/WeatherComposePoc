package model.core

import androidx.compose.runtime.compositionLocalOf

interface Logger {
    fun error(tag: String, message: String)
    fun warning(tag: String, message: String)
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun verbose(tag: String, message: String)
}

class LoggerImpl: Logger {
    private fun log(tag: String, message: String) {
        println("$tag: $message")
    }
    override fun error(tag: String, message: String) {
        log(tag, message)
    }

    override fun warning(tag: String, message: String) {
        log(tag, message)
    }

    override fun debug(tag: String, message: String) {
        log(tag, message)
    }

    override fun info(tag: String, message: String) {
        log(tag, message)
    }

    override fun verbose(tag: String, message: String) {
        log(tag, message)
    }
}
