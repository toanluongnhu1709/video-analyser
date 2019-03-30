package com.boclips.videoanalyser.config.messaging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

interface TestTopics {

    @Output("first-topic")
    fun oneTopic(): MessageChannel

    @Output("second-topic")
    fun anotherTopic(): MessageChannel
}

interface TestSubscriptions {

    @Input("one-subscription")
    fun firstSubscription(): SubscribableChannel

    @Input("another-subscription")
    fun anotherSubscription(): SubscribableChannel
}

interface InvalidChannelNames {

    @Output("first")
    fun oneTopic(): MessageChannel

    @Input("one")
    fun firstSubscription(): SubscribableChannel
}

class TopicDetectorTest {

    @Test
    fun `extract topics from topic channels`() {
        val topicNames = TopicDetector.scanTopicChannels(TestTopics::class)

        assertThat(topicNames.map { it.topicName }).containsExactlyInAnyOrder("first", "second")
        assertThat(topicNames.map { it.channelName }).containsExactlyInAnyOrder("first-topic", "second-topic")
    }

    @Test
    fun `extract topics from subscription channels`() {
        val topicNames = TopicDetector.scanSubscriptionChannels(TestSubscriptions::class)

        assertThat(topicNames.map { it.topicName }).containsExactlyInAnyOrder("one", "another")
        assertThat(topicNames.map { it.channelName }).containsExactlyInAnyOrder("one-subscription", "another-subscription")
    }

    @Test
    fun `throws when topic channel lacks the expected suffix`() {
        assertThrows<Exception> {
            TopicDetector.scanTopicChannels(InvalidChannelNames::class)
        }
    }

    @Test
    fun `throws when subscription channel lacks the expected suffix`() {
        assertThrows<Exception> {
            TopicDetector.scanSubscriptionChannels(InvalidChannelNames::class)
        }
    }
}
