package com.bubul.col.server.friends

import com.bubul.col.messages.friend.*
import com.bubul.col.server.db.*
import com.bubul.col.server.net.mqtt.MessageListener
import com.bubul.col.server.net.mqtt.MqttClient
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

class FriendsManager(val mqttClient: MqttClient) {
    fun connect() {
        mqttClient.subscribe(FriendStatusRequestMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = FriendStatusRequestMsg.deserialize(message)
                val friendMap = mutableMapOf<String, Pair<FriendStatus, String>>()
                val friendRequestList = mutableListOf<String>()
                withBdd {
                    for (data in Friend.find { (Friends.asker eq msg.source) or (Friends.accepter eq msg.source) }) {
                        val friendName = if (data.asker == msg.source) data.accepter else data.asker
                        val accountList = Account.find { Accounts.login eq friendName }
                        if (accountList.count() > 0) {
                            val friendAccount = accountList.elementAt(0)
                            var status = FriendStatus.Offline
                            when (friendAccount.status) {
                                AccountStatus.Offline.value -> {
                                    status = FriendStatus.Offline
                                }
                                AccountStatus.Online.value -> {
                                    status = FriendStatus.Online
                                }
                                AccountStatus.Playing.value -> {
                                    status = FriendStatus.Playing
                                }
                            }
                            friendMap[friendName] = Pair(status, friendAccount.clientID)
                        }
                    }
                    for (data in FriendRequest.find { FriendRequests.accepter eq msg.source }) {
                        friendRequestList.add(data.asker)
                    }
                }
                val resMsg = FriendStatusListMsg(msg.source, friendMap)
                mqttClient.publish(resMsg)
                val resRequestsMsg = FriendRequestListMsg(msg.entity, friendRequestList)
                mqttClient.publish(resRequestsMsg)
            }

        })
        mqttClient.subscribe(FriendRequestMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = FriendRequestMsg.deserialize(message)
                withBdd {
                    FriendRequest.new {
                        asker = msg.source
                        accepter = msg.target
                    }
                }
            }

        })
        mqttClient.subscribe(FriendResponseMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = FriendResponseMsg.deserialize(message)
                withBdd {
                    for (data in FriendRequest.find { FriendRequests.asker eq msg.target }) {
                        data.delete()
                    }
                    if (msg.accepted) {
                        val alreadyRegisterFirendLink = Friend.find {
                            ((Friends.asker eq msg.source) and (Friends.accepter eq msg.target)) or
                                    ((Friends.asker eq msg.target) and (Friends.accepter eq msg.source))
                        }
                        if (alreadyRegisterFirendLink.empty()) {
                            Friend.new {
                                asker = msg.target
                                accepter = msg.source
                            }
                            //Send the confirmation to the sender
                            var friendClientId = ""
                            val accountList = Account.find { Accounts.login eq msg.target }
                            if (accountList.count() > 0)
                                friendClientId = accountList.elementAt(0).clientID
                            val msgResponse = FriendResponseMsg(msg.target, msg.source, msg.accepted, friendClientId)
                            mqttClient.publish(msgResponse)
                        }
                    }
                }
            }

        })
        mqttClient.subscribe(FriendRemovalMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = FriendRemovalMsg.deserialize(message)
                withBdd {
                    for (data in Friend.find { (Friends.asker eq msg.source) and (Friends.accepter eq msg.target) }) {
                        data.delete()
                    }
                    for (data in Friend.find { (Friends.asker eq msg.target) and (Friends.accepter eq msg.source) }) {
                        data.delete()
                    }
                }
            }
        })
    }

    fun disconnect() {
        mqttClient.unsubscribe(FriendRequestMsg.topic)
        mqttClient.unsubscribe(FriendResponseMsg.topic)
        mqttClient.unsubscribe(FriendRemovalMsg.topic)
    }
}