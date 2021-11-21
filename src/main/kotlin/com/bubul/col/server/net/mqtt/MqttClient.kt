package com.bubul.col.server.net.mqtt

import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import com.bubul.col.messages.MqttMessage

interface MessageListener {
    fun messageArrived(message : ByteArray)
}

class MqttClient(val clientId : String) {

    private val internalClient = MqttClient("tcp://cardsoflegendsupdate.bubul.ovh:1883", clientId, MemoryPersistence())

    fun connect() {
        internalClient.connect()
    }

    fun publish(msg : MqttMessage) {
        val sentMsg = org.eclipse.paho.client.mqttv3.MqttMessage(msg.serialize())
        sentMsg.qos = 2
        internalClient.publish(msg.getMqttTopic(), sentMsg)
    }

    fun subscribe(topic: String, listener : MessageListener) {
        internalClient.subscribe(topic, object : IMqttMessageListener {
            override fun messageArrived(receivedTopic: String?, message: org.eclipse.paho.client.mqttv3.MqttMessage?) {
                if(topic == receivedTopic)
                {
                    listener.messageArrived(message!!.payload)
                }
            }

        })
    }

    fun unsubscribe(topic: String) {
        internalClient.unsubscribe(topic)
    }

    fun disconnect() {
        internalClient.disconnect()
    }

}