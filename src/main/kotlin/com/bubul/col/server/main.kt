package com.bubul.col.server

import kotlin.system.exitProcess

import sun.misc.Signal
import sun.misc.SignalHandler

fun main() {
    val colServer = COLServer()
    Signal.handle(Signal("INT"), object : SignalHandler {
        override fun handle(p0: Signal?) {
            colServer.stop()
            exitProcess(0)
        }

    })
    colServer.start()
}