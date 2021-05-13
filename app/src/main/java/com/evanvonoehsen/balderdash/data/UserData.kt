package com.evanvonoehsen.balderdash.data

data class UserData (
        val pastWords: MutableList<String> = mutableListOf(),
        val username: String = ""
)