package com.evanvonoehsen.balderdash.data

data class PendingGame (
    var code: String = "",
    var players: MutableList<String> = mutableListOf(),
    var playerUsernames: MutableList<String> = mutableListOf(),
    var started: Boolean = false)