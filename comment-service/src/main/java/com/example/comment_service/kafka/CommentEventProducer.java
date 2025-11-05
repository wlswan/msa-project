package com.example.comment_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventProducer {

    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void send(CommentEvent event) {
        log.info("Kafka 전송: {}", event);
        kafkaTemplate.send("comment-events",event.getPostId().toString(), event)
                .whenComplete((result,ex) -> {
                    RecordMetadata recordMetadata = result.getRecordMetadata();
                    if(ex == null) {
                        log.info("전송 성공! topic:{}, partition:{},offset:{}"
                                ,recordMetadata.topic()
                                ,recordMetadata.partition()
                                ,recordMetadata.offset());
                    }
                    else {
                        log.error("전송 실패! : {}", ex.getMessage());
                    }
                });

    }
}
