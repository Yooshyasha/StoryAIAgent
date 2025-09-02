package com.yooshyasha.storyaiagent.agent.common

import ai.koog.agents.core.agent.AIAgent

interface IAgentProvider<Output> {
    fun providerAgent(
        handleException: suspend (String) -> Unit,
    ): AIAgent<String, Output>
}