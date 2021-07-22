package curfew.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/** Created by manish.shukla on 2020/4/2. */
@Service
public class SimpleProducer {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public SimpleProducer(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void postObjectInTopic(String topicName, String payload) {
    kafkaTemplate.send(topicName, payload);
  }

  public void postObjectInDefaultTopic(String key, String value) {
    kafkaTemplate.send(kafkaTemplate.getDefaultTopic(), key, value);
  }

  public <T> void postObjectInDefaultTopic(String key, T payload) {
    kafkaTemplate.send(kafkaTemplate.getDefaultTopic(), key, payload);
  }
}
