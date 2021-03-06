package com.bubul.col.server.net.ping

import com.bubul.col.messages.ping.PingMsg
import com.bubul.col.messages.ping.PongMsg
import com.bubul.col.server.net.mqtt.MessageListener
import com.bubul.col.server.net.mqtt.MqttClient
import java.util.*

data class pingValue(val ping : Long, val date : Date)

class PingManager(val entityId : String, val mqttClient: MqttClient) {

    fun init() {
        mqttClient.subscribe(PingMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val ping = PingMsg.deserialize(message)
                if (ping.targetEntity == entityId) {
                    mqttClient.publish(PongMsg(entityId, ping.sourceEntity, ping.time))
                }
            }

        })
    }
}