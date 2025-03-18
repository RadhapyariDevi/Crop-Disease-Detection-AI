package com.radhapyari.ai_for_crop_diseases_detection

data class ChatbotResponse(
    val answer: String,
    val sources: List<Source> // List of sources (optional)
)
data class Source(
    val id: String, // Unique identifier
    val metadata: Metadata, // Metadata about the source
    val page_content: String, // Content of the source
    val type: String // Type of the source (e.g., "Document")
)
data class Metadata(
    val producer: String, // Producer of the document
    val creator: String, // Creator of the document
    val creationdate: String, // Creation date
    val author: String, // Author of the document
    val moddate: String, // Last modification date
    val source: String, // Source file path
    val total_pages: Int, // Total number of pages
    val page: Int, // Page number
    val page_label: String // Page label
)