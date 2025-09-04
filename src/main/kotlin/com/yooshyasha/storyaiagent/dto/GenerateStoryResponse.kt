package com.yooshyasha.storyaiagent.dto

data class GenerateStoryResponse(
    val title: String,
    val scenes: List<Scene>,
    val totalDuration: Int,
)
