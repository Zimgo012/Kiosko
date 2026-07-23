package com.example.myapplication.engine.printer

enum class EventNameStyle(val displayName: String) {
    RETRO("Retro"),
    CURSIVE("Cursive"),
    MODERN("Modern"),
    NORMAL("Normal")
}

data class PrintTemplateSettings(
    val eventName: String = "Sarah's 18th Birthday",
    val eventDescription: String = "Celebrating life and love",
    val eventStyle: EventNameStyle = EventNameStyle.RETRO,
    val boothName: String = "Rollie Photo Booth",
    val phoneNumber: String = "+1 234 567 890",
    val email: String = "hello@rollie.com"
)
