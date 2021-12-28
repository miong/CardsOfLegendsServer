package com.bubul.col.server.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Paths
import java.sql.Connection

class DbManager {

    private lateinit var db: Database

    fun connect() {
        val dbPath = Paths.get("accounts.db")
        db = Database.connect("jdbc:sqlite:$dbPath", "org.sqlite.JDBC")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Accounts,
                Friends,
                FriendRequests
            )
        }
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE
    }

    fun disconnect() {
        db.let { }
    }
}

fun withBdd(block: () -> Unit) {
    transaction {
        block()
    }
}