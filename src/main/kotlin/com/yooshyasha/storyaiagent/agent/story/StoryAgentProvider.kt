package com.yooshyasha.storyaiagent.agent.story

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMCompressHistory
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.yooshyasha.storyaiagent.agent.common.IAgentProvider
import com.yooshyasha.storyaiagent.dto.GenerateStoryResponse
import org.springframework.beans.factory.annotation.Value

class StoryAgentProvider(
    @Value("\${ai.koog.openai.api-key}") private val openAiApiKey: String,
) : IAgentProvider<GenerateStoryResponse> {
    private val objectMapper = jacksonObjectMapper()

    override fun providerAgent(handleException: suspend (String) -> Unit): AIAgent<String, GenerateStoryResponse> {
        val strategy = strategy<String, GenerateStoryResponse>("story-generation-strategy") {
            val nodeGenerateStory by nodeLLMRequest("generateStory")
            val nodeReviewStory by nodeLLMRequest("reviewStory")
            val nodeStructureStory by nodeLLMRequest("structureStory")
            val nodeCompression by nodeLLMCompressHistory<String>()

            val nodeFinalLoad by node<String, GenerateStoryResponse>("finalLoadNode") {
                return@node objectMapper.reader().readValue<GenerateStoryResponse>(it)
            }

            edge(nodeStart forwardTo nodeGenerateStory)

            edge(nodeGenerateStory forwardTo nodeReviewStory transformed { it.content })

            edge(nodeReviewStory forwardTo nodeStructureStory onAssistantMessage {
                !it.content.contains("need story revision")
            })
            edge(nodeReviewStory forwardTo nodeCompression onAssistantMessage {
                it.content.contains("need story revision")
            })

            edge(nodeCompression forwardTo nodeGenerateStory)

            edge(nodeStructureStory forwardTo nodeFinalLoad transformed { it.content })

            edge(nodeFinalLoad forwardTo nodeFinish)
        }

        val agentConfig = AIAgentConfig(
            prompt = prompt("story-generation") {
                system(
                    """
                    Ты AI-агент для создания видео-историй. Работаешь в 3 этапа с четкими критериями.
                    
                    ЭТАП 1: ГЕНЕРАЦИЯ ИСТОРИИ
                    Задача: Создать историю строго по теме пользователя
                    
                    ОБЯЗАТЕЛЬНЫЕ требования:
                    Длина: ровно 400-600 слов
                    Структура: завязка (100 слов) → развитие (300 слов) → кульминация + развязка (200 слов)
                    Каждый абзац = одна сцена (максимум 50 слов на абзац)
                    Используй только визуальные описания (то, что можно показать в видео)
                    Избегай внутренних монологов и размышлений персонажей
                    
                    ФОРМАТ ответа: только текст истории, без комментариев.
                    
                    ЭТАП 2: КОНТРОЛЬ КАЧЕСТВА
                    Задача: Проверить историю по чек-листу
                    
                    ПРОВЕРЬ:
                    1. Длина 400-600 слов? (подсчитай)
                    2. Есть четкое начало-середина-конец?
                    3. Все действия визуально показуемые?
                    4. Максимум 12 абзацев?
                    5. Соответствует теме пользователя?
                    
                    ЕСЛИ хотя бы один пункт НЕТ:
                    Начни ответ с "need story revision: [номер пункта]" + объяснение проблемы
                    
                    ЕСЛИ все ОК:
                    Напиши точно: "story approved" + перешли историю без изменений
                    
                    ЭТАП 3: РАЗБИВКА НА СЦЕНЫ
                    Задача: Конвертировать в JSON для видео-генерации
                    
                    ДЕЙСТВИЯ:
                    1. Раздели каждый абзац = отдельная сцена
                    2. Для каждой сцены извлеки:
                       - action: что происходит (одним предложением)
                       - visual: описание обстановки, персонажей, объектов
                       - duration: 8-15 секунд на сцену
                    3. Создай заголовок (3-5 слов)
                    
                    СТРОГИЙ JSON формат:
                    {
                        "title": "Заголовок истории",
                        "totalDuration": "общая длительность в секундах",
                        "scenes": [
                            {
                                "sceneNumber": 1,
                                "action": "описание действия",
                                "visual": "визуальные детали для ИИ-генерации",
                                "duration": 10
                            }
                        ]
                    }
                    
                    ОБРАБОТКА ОШИБОК:
                    Если тема неясна - запроси уточнение
                    Если тема неподходящая - предложи альтернативу
                    Если история слишком сложная - упрости
                    
                    Работай пошагово. Один этап = один ответ.
                """.trimIndent()
                )
            },
            model = OpenAIModels.Chat.GPT4o,
            maxAgentIterations = 20,
        )

        return AIAgent(
            promptExecutor = simpleOpenAIExecutor(openAiApiKey),
            strategy = strategy,
            agentConfig = agentConfig,
        )
        {
            handleEvents {
                onAgentRunError { handleException("${it.throwable.message}") }
            }
        }
    }
}