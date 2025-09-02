package com.yooshyasha.storyaiagent.agent.story

import ai.koog.agents.core.agent.AIAgent
import com.yooshyasha.storyaiagent.agent.common.IAgentProvider
import com.yooshyasha.storyaiagent.dto.GenerateStoryResponse

class StoryAgentProvider : IAgentProvider<GenerateStoryResponse> {
    override fun providerAgent(handleException: suspend (String) -> Unit): AIAgent<String, GenerateStoryResponse> {
        TODO("Not yet implemented")
    }
}