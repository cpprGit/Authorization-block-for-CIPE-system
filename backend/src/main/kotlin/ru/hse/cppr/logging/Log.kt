package ru.hse.cppr.logging

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.File

class Log(val tag: String) {

    private val itTag = "[INTEGRATION TESTING]"

    private val sdfTime   = DateTimeFormat.forPattern("MM.dd.yyyy HH:mm:ss.SSS")
    private val logFile   = File(File("logs").apply { mkdirs() }, "execution.log").apply { if (!exists()) createNewFile() }

    fun testRun(message: String) {
        log(itTag, message, ANSIColor.BLUE.regular)
        logFile.appendText("${ANSIColor.WHITE.regular}${DateTime.now().toString(sdfTime)}/${makeTag("$tag: ")}$message${ANSIColor.RESET}\n")
    }

    fun testStatus(message: String) {
        log(itTag, message, ANSIColor.YELLOW.regular)
        logFile.appendText("${ANSIColor.WHITE.regular}${DateTime.now().toString(sdfTime)}/${makeTag("$tag: ")}$message${ANSIColor.RESET}\n")
    }

    fun testSuccess(message: String) {
        log(itTag, message, ANSIColor.GREEN.regular)
        logFile.appendText("${ANSIColor.WHITE.regular}${DateTime.now().toString(sdfTime)}/${makeTag("$tag: ")}$message${ANSIColor.RESET}\n")
    }

    fun testFailure(message: String) {
        log(itTag, message, ANSIColor.RED.regular)
        logFile.appendText("${ANSIColor.WHITE.regular}${DateTime.now().toString(sdfTime)}/${makeTag("$tag: ")}$message${ANSIColor.RESET}\n")
    }


    fun i(message: String) {
        log(tag, message, ANSIColor.WHITE.regular)
        logFile.appendText("${ANSIColor.WHITE.regular}${DateTime.now().toString(sdfTime)}/${makeTag("$tag: ")}$message${ANSIColor.RESET}\n")
    }

    fun d(message: String) {
        log(tag, message, ANSIColor.BLUE.regular)
        logFile.appendText("${ANSIColor.BLUE.regular}${DateTime.now().toString(sdfTime)}/${makeTag("$tag: ")}$message${ANSIColor.RESET}\n")
    }

    fun w(message: String) {
        log(tag, message, ANSIColor.YELLOW.regular)
        logFile.appendText("${ANSIColor.YELLOW.regular}${DateTime.now().toString(sdfTime)}/${makeTag("$tag: ")}$message${ANSIColor.RESET}\n")
    }

    fun e(message: String) {
        log(tag, message, ANSIColor.RED.regular)
        logFile.appendText("${ANSIColor.RED.regular}${DateTime.now().toString(sdfTime)}/${makeTag("$tag: ")}$message${ANSIColor.RESET}\n")
    }

    private fun log(tag: String, message: String, color: String) {
        println("$color${DateTime.now().toString(sdfTime)}/${makeTag("$tag: ")}$message${ANSIColor.RESET}")
    }

    private fun makeTag(tag: String): String {
        val local = if (32 < tag.length) {
            tag.substring(0, 32)
        } else {
            tag
        }

        return String.format("%-" + 28 + "s", local)
    }

    private enum class ANSIColor(private val colorNumber: Byte) {
        BLACK(0), RED(1), GREEN(2), YELLOW(3), BLUE(4), MAGENTA(5), CYAN(6), WHITE(7);

        companion object {
            private const val prefix = "\u001B"
            const val RESET = "$prefix[0m"
            private val isCompatible = "win" !in System.getProperty("os.name").toLowerCase()
        }

        val regular get() = if (isCompatible) "$prefix[0;3${colorNumber}m" else ""
        val bold get() = if (isCompatible) "$prefix[1;3${colorNumber}m" else ""
        val underline get() = if (isCompatible) "$prefix[4;3${colorNumber}m" else ""
        val background get() = if (isCompatible) "$prefix[4${colorNumber}m" else ""
        val highIntensity get() = if (isCompatible) "$prefix[0;9${colorNumber}m" else ""
        val boldHighIntensity get() = if (isCompatible) "$prefix[1;9${colorNumber}m" else ""
        val backgroundHighIntensity get() = if (isCompatible) "$prefix[0;10${colorNumber}m" else ""
    }
}
