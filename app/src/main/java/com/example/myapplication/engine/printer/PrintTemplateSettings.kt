package com.example.myapplication.engine.printer

enum class EventNameStyle(val displayName: String) {
    RETRO("Retro"),
    CURSIVE("Cursive"),
    MODERN("Modern"),
    NORMAL("Normal")
}

data class PrintTemplateSettings(
    val eventDescription: String = "Celebrating life and love",
    val eventStyle: EventNameStyle = EventNameStyle.RETRO,
    val topText: String = "Sarah's",
    val bottomText: String = "18th Birthday",
    val boothName: String = "Rollie Photo Booth",
    val phoneNumber: String = "+1 234 567 890",
    val email: String = "hello@rollie.com",
    val clientFolderName: String = "Sarahs_18th_Birthday",
    val ditherAutoAdjust: Boolean = true,
    val ditherBrightnessShift: Float = 0f,
    val ditherContrast: Float = 1.35f,
    val ditherGamma: Float = 0.85f,
    val cameraExposure: Int = 0
)
