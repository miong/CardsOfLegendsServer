package com.bubul.col.server

import sun.misc.Signal
import kotlin.system.exitProcess

fun main() {
    val colServer = COLServer()
    Signal.handle(Signal("INT")) {
        colServer.stop()
        exitProcess(0)
    }
    colServer.start()
}