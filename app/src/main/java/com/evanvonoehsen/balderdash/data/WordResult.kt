package com.evanvonoehsen.balderdash.data

data class WordResult(val _id: String?, val word: String?, val contentProvider: ContentProvider?, val definitions: List<Definition>?, val publishDate: String?, val examples: List<Example>?, val pdd: String?, val htmlExtra: Any?, val note: String?)

data class ContentProvider(val name: String?, val id: Number?)

data class Definition(val source: String?, val text: String?, val note: Any?, val partOfSpeech: String?)

data class Example(val url: String?, val title: String?, val text: String?, val id: Number?)