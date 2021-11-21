package com.bubul.col.server

import com.bubul.col.server.net.NetManager
import org.slf4j.LoggerFactory

class COLServer {

    private val netManager = NetManager("COLServer")
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun start() {
        logger.info("Starting server")
        netManager.connect()
        logger.info("Server started and ready")
    }

    fun stop() {
        logger.info("Stopping server")
        netManager.disconnect()
        logger.info("Server stopped")
    }
}