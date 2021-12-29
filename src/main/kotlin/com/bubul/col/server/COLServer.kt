package com.bubul.col.server

import com.bubul.col.server.db.DbManager
import com.bubul.col.server.friends.FriendsManager
import com.bubul.col.server.login.LoginManager
import com.bubul.col.server.net.NetManager
import org.slf4j.LoggerFactory

class COLServer {

    private val dbManager = DbManager()
    private val netManager = NetManager("COLServer")
    private val loginManager = LoginManager(netManager.getMqttClient())
    private val friendsManager = FriendsManager(netManager.getMqttClient())
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun start() {
        logger.info("Starting server")
        dbManager.connect()
        netManager.connect()
        loginManager.connect()
        friendsManager.connect()
        logger.info("Server started and ready")
    }

    fun stop() {
        logger.info("Stopping server")
        try {
            friendsManager.disconnect()
            loginManager.disconnect()
            netManager.disconnect()
            dbManager.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        logger.info("Server stopped")
    }
}