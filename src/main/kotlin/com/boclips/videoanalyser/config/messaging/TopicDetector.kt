package com.boclips.videoanalyser.config.messaging

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import java.lang.RuntimeException
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

data class TopicInfo(val topicName: String, val channelName: String) {

    companion object {
        fun fromChannelName(channelName: String, channelSuffix: String): TopicInfo {
            if(!channelName.endsWith(channelSuffix)) {
                throw RuntimeException("Expected suffix $channelSuffix for channel $channelName")
            }

            val topicName = channelName.replace(Regex("$channelSuffix\$"), "")

            return TopicInfo(topicName, channelName)
        }
    }
}

object TopicDetector {

    fun scanTopicChannels(kClass: KClass<*>): Set<TopicInfo> {
        return kClass.functions
                .mapNotNull { it.findAnnotation<Output>() }
                .map { it.value }
                .map { TopicInfo.fromChannelName(it, "-topic") }
                .toSet()
    }

    fun scanSubscriptionChannels(kClass: KClass<*>): Set<TopicInfo> {
        return kClass.functions
                .mapNotNull { it.findAnnotation<Input>() }
                .map { it.value }
                .map { TopicInfo.fromChannelName(it, "-subscription") }
                .toSet()
    }
}
