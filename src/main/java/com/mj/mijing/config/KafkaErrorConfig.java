package com.mj.mijing.config;

import com.mj.mijing.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import javax.annotation.Resource;

/**
 * Kafka 消费端错误处理配置
 * <p>
 * 策略：消费失败后重试 3 次（间隔 1 秒），若仍失败则将消息投递到死信 Topic（DLT）
 * 死信 Topic 由 {@link com.mj.mijing.kafka.VoucherOrderDltConsumer} 进行人工/自动补偿
 */
@Slf4j
@Configuration
public class KafkaErrorConfig {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 配置全局消费者错误处理器
     * - 最多重试 3 次，每次间隔 1000ms
     * - 重试耗尽后，消息发送到死信 Topic
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        // 死信队列发布器：失败消息自动转发到 原Topic + ".DLT" 后缀的 Topic
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (ConsumerRecord<?, ?> record, Exception ex) -> {
                    log.error("消息消费失败，即将投递到死信队列，topic={}, key={}, error={}",
                            record.topic(), record.key(), ex.getMessage());
                    return new org.apache.kafka.common.TopicPartition(
                            SystemConstants.TOPIC_VOUCHER_ORDER_DLT, record.partition());
                });

        // FixedBackOff(间隔ms, 最大重试次数)
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer,
                new FixedBackOff(1000L, 3L));

        // 记录每次重试的日志
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("消费重试中，topic={}, key={}, attempt={}, error={}",
                        record.topic(), record.key(), deliveryAttempt, ex.getMessage()));

        return errorHandler;
    }
}
