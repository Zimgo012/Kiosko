package com.example.myapplication.engine.printer

data class PrintTemplateSettings(
    val logoText: String = "Rollie Photo Booth",
    val phoneNumber: String = "+1 234 567 890",
    val email: String = "hello@rollie.com",
    val message: String = "Thank you for coming!",
    val description: String = "Share your photos with #RollieBooth"
)
