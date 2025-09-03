package com.yooshyasha.storyaiagent.agent.story

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.yooshyasha.storyaiagent.agent.common.IAgentProvider
import com.yooshyasha.storyaiagent.dto.GenerateStoryResponse
import org.springframework.beans.factory.annotation.Value

class StoryAgentProvider(
    @Value("\${ai.koog.openai.api-key}") private val openAiApiKey: String,
) : IAgentProvider<GenerateStoryResponse> {
    override fun providerAgent(handleException: suspend (String) -> Unit): AIAgent<String, GenerateStoryResponse> {
        val strategy = strategy<String, GenerateStoryResponse>("generateStoryStrategy") {

        }

        val agentConfig = AIAgentConfig(
            prompt = prompt("storyPrompt") {
                system("")
            },
            model = OpenAIModels.Chat.GPT4o,
            maxAgentIterations = 50,
        )

        return AIAgent(
            promptExecutor = simpleOpenAIExecutor(openAiApiKey),
            strategy = strategy,
            agentConfig = agentConfig,
        ) {
            handleEvents {
                onAgentRunError { handleException("${it.throwable.message}") }
            }
        }
    }
}