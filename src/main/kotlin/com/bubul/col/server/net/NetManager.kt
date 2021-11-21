package com.bubul.col.server.net

import com.bubul.col.server.net.mqtt.MqttClient
import com.bubul.col.server.net.ping.PingManager

class NetManager(val clientId : String) {

    private val mqttClient = MqttClient(clientId)
    private val pingManager = PingManager(clientId, mqttClient)

    fun connect() {
        mqttClient.connect()
        pingManager.init()
    }

    fun disconnect() {
        mqttClient.disconnect()
    }
}