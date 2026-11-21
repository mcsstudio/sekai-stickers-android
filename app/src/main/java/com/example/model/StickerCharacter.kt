package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class DefaultText(
    val text: String,
    val x: Float,
    val y: Float,
    val r: Float,
    val s: Float,
)

@Serializable
data class StickerCharacter(
    val id: String,
    val name: String,
    val character: String,
    val img: String,
    val color: String,
    val defaultText: DefaultText,
)
