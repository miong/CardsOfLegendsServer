package com.bubul.col.server.login

import com.bubul.col.messages.login.*
import com.bubul.col.server.db.Account
import com.bubul.col.server.db.AccountStatus
import com.bubul.col.server.db.Accounts
import com.bubul.col.server.db.withBdd
import com.bubul.col.server.net.mqtt.MessageListener
import com.bubul.col.server.net.mqtt.MqttClient
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

class LoginManager(val mqttClient: MqttClient) {

    private lateinit var db: Database
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun connect() {
        mqttClient.subscribe(LoginMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val loginMsg = LoginMsg.deserialize(message)
                var res = LoginResultItem.Failed
                withBdd {
                    val accountList = Account.find { Accounts.login eq loginMsg.login }
                    if (accountList.count() > 0) {
                        val account = accountList.elementAt(0)
                        if (loginMsg.mdpSalt == account.salt && account.clientID.isEmpty()) {
                            account.clientID = loginMsg.sourceEntity
                            account.status = AccountStatus.Online.value
                            res = LoginResultItem.Success
                        } else {
                            account.status = AccountStatus.Offline.value
                        }
                    }
                }
                logger.info("Login attempt for ${loginMsg.login} result is $res")
                val response = LoginRestultMsg(loginMsg.sourceEntity, loginMsg.login, res)
                mqttClient.publish(response)
            }

        })
        mqttClient.subscribe(RegisterMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val registerMsg = RegisterMsg.deserialize(message)
                var res = LoginResultItem.Failed
                withBdd {
                    val accountList = Account.find { Accounts.login eq registerMsg.login }
                    if (accountList.empty()) {
                        Account.new {
                            login = registerMsg.login
                            clientID = registerMsg.sourceEntity
                            salt = registerMsg.mdpSalt
                            status = AccountStatus.Online.value
                        }
                        res = LoginResultItem.Success
                    }
                }
                logger.info("Registering attempt for ${registerMsg.login} result is $res")
                val response = RegisterRestulMsg(registerMsg.sourceEntity, registerMsg.login, res)
                mqttClient.publish(response)
            }

        })
        mqttClient.subscribe(LogoutMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val logoutMsg = LogoutMsg.deserialize(message)
                withBdd {
                    for (data in Account.find { Accounts.login eq logoutMsg.login }) {
                        if (data.clientID == logoutMsg.sourceEntity) {
                            data.clientID = ""
                            data.status = AccountStatus.Offline.value
                            logger.info("Logout ${logoutMsg.login}")
                        }
                    }
                }
            }
        })
    }

    fun disconnect() {
        mqttClient.unsubscribe(LoginMsg.topic)
        mqttClient.unsubscribe(RegisterMsg.topic)
        mqttClient.unsubscribe(LogoutMsg.topic)
    }
}