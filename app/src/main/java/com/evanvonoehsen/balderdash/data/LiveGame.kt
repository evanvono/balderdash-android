package com.evanvonoehsen.balderdash.data

data class LiveGame (
        var host: String = "",
        var chooser: String = "",
        var players: List<String> = mutableListOf(),
        var playerUsernames: List<String> = mutableListOf(),
        var pointTotals: MutableMap<String,Long> = mutableMapOf<String,Long>(),
        var selectedWord: String = "",
        var correctDefiniton: String = "",
        var playerDefinitions: MutableMap<String,String> = mutableMapOf<String,String>(),
        var pastWords: MutableList<String> = mutableListOf(),
        var readyForResults: Long = 0
)