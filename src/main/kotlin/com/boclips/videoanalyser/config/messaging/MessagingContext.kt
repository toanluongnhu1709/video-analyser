package com.boclips.videoanalyser.config.messaging

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.config.BindingProperties
import org.springframework.cloud.stream.config.BindingServiceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@EnableBinding(Topics::class, Subscriptions::class)
class MessagingContext(@Value("\${pubsub.topic.suffix}") val topicSuffix: String) {

    @Primary
    @Bean
    fun bindingServiceProperties(): BindingServiceProperties {

        val bindings = mutableMapOf<String, BindingProperties>()

        TopicDetector.scanTopicChannels(Topics::class).forEach { topic ->
            bindings[topic.channelName] = topicBindingProperties(topic.topicName)
        }

        TopicDetector.scanSubscriptionChannels(Subscriptions::class).forEach { topic ->
            bindings[topic.channelName] = subscriptionBindingProperties(topic.topicName)
        }

        return BindingServiceProperties().apply {
            this.bindings = bindings
        }
    }

    private fun bindingProperties(topicName: String) = BindingProperties().apply {
        destination = "$topicName-$topicSuffix"
    }

    private fun topicBindingProperties(topicName: String) = bindingProperties(topicName)

    private fun subscriptionBindingProperties(topicName: String) =
            bindingProperties(topicName).apply {
                group = "video-analyser"
            }
}
