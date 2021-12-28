package com.bubul.col.server.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

enum class AccountStatus(val value: Int) {
    Offline(0),
    Online(1),
    Playing(2)
}

object Accounts : IntIdTable() {
    val login = varchar("login", 250)
    val salt = varchar("salt", 256)
    val clientID = varchar("clientID", 50)
    val status = integer("status").default(0)
}

class Account(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Account>(Accounts)

    var login by Accounts.login
    var salt by Accounts.salt
    var clientID by Accounts.clientID
    var status by Accounts.status
}

object Friends : IntIdTable() {
    val asker = varchar("asker", 250)
    val accepter = varchar("accepter", 250)
}

class Friend(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Friend>(Friends)

    var asker by Friends.asker
    var accepter by Friends.accepter
}

object FriendRequests : IntIdTable() {
    val asker = varchar("asker", 250)
    val accepter = varchar("accepter", 250)
}

class FriendRequest(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FriendRequest>(FriendRequests)

    var asker by FriendRequests.asker
    var accepter by FriendRequests.accepter
}