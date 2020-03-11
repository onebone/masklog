package me.onebone.masklog

data class RegisteredLocation (
    val location: String,
    val users: MutableList<Receiver>
)

data class Receiver (
    val userId: Int,
    val chatId: Long
)
