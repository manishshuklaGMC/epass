package curfew.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Created by manish.shukla on 2020/4/2. */
@Service
public class ConfigBeans {

  @Autowired private KafkaProperties kafkaProperties;

  @Value("${tpd.topic-name}")
  private String topicName;

  @Value("${tpd.numberOfPartitions}")
  private int numPartitions;

  @Value("${tpd.replicationFactor}")
  private short replicationFactor;

  @Bean
  public ConcurrentKafkaListenerContainerFactoryConfigurer kafkaListenerContainerFactoryConfigurer(
      KafkaProperties kafkaProperties,
      ObjectProvider<RecordMessageConverter> messageConverterObjectProvider,
      ObjectProvider<KafkaTemplate<Object, Object>> kafkaTemplateObjectProvider) {

    RecordMessageConverter messageConverter = messageConverterObjectProvider.getIfUnique();
    KafkaTemplate<Object, Object> kafkaTemplate = kafkaTemplateObjectProvider.getIfUnique();

    return new ConcurrentKafkaListenerContainerFactoryConfigurer() {

      @Override
      public void configure(
          ConcurrentKafkaListenerContainerFactory<Object, Object> listenerFactory,
          ConsumerFactory<Object, Object> consumerFactory) {

        listenerFactory.setConsumerFactory(consumerFactory);
        configureListenerFactory(listenerFactory);
        configureContainer(listenerFactory.getContainerProperties());
      }

      private void configureListenerFactory(
          ConcurrentKafkaListenerContainerFactory<Object, Object> factory) {
        PropertyMapper map = PropertyMapper.get();
        KafkaProperties.Listener properties = kafkaProperties.getListener();
        map.from(properties::getConcurrency).whenNonNull().to(factory::setConcurrency);
        map.from(() -> messageConverter).whenNonNull().to(factory::setMessageConverter);
        map.from(() -> kafkaTemplate).whenNonNull().to(factory::setReplyTemplate);
        map.from(properties::getType)
            .whenEqualTo(KafkaProperties.Listener.Type.BATCH)
            .toCall(() -> factory.setBatchListener(true));
      }

      private void configureContainer(ContainerProperties container) {
        PropertyMapper map = PropertyMapper.get();
        KafkaProperties.Listener properties = kafkaProperties.getListener();
        map.from(properties::getAckMode).whenNonNull().to(container::setAckMode);
        map.from(properties::getAckCount).whenNonNull().to(container::setAckCount);
        map.from(properties::getAckTime)
            .whenNonNull()
            .as(Duration::toMillis)
            .to(container::setAckTime);
        map.from(properties::getPollTimeout)
            .whenNonNull()
            .as(Duration::toMillis)
            .to(container::setPollTimeout);
        map.from(properties::getNoPollThreshold).whenNonNull().to(container::setNoPollThreshold);
        map.from(properties::getIdleEventInterval)
            .whenNonNull()
            .as(Duration::toMillis)
            .to(container::setIdleEventInterval);
        map.from(properties::getMonitorInterval)
            .whenNonNull()
            .as(Duration::getSeconds)
            .as(Number::intValue)
            .to(container::setMonitorInterval);
      }
    };
  }

  // Producer configuration

  @Bean
  public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate() {
    KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory());
    kafkaTemplate.setDefaultTopic(topicName);
    return kafkaTemplate;
  }

  @Bean
  public NewTopic createTopic() {
    return new NewTopic(topicName, numPartitions, replicationFactor);
  }

  @Bean
  public ConsumerFactory<String, Object> consumerFactory() {
    final JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(Object.class);
    return new DefaultKafkaConsumerFactory<>(
        kafkaProperties.buildConsumerProperties(), new StringDeserializer(), jsonDeserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());

    return factory;
  }

  @Bean
  public ConsumerFactory<String, String> stringConsumerFactory() {
    return new DefaultKafkaConsumerFactory<>(
        kafkaProperties.buildConsumerProperties(),
        new StringDeserializer(),
        new StringDeserializer());
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String>
      kafkaListenerStringContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(stringConsumerFactory());

    return factory;
  }

  @Bean
  public ConsumerFactory<String, byte[]> byteArrayConsumerFactory() {
    return new DefaultKafkaConsumerFactory<>(
        kafkaProperties.buildConsumerProperties(),
        new StringDeserializer(),
        new ByteArrayDeserializer());
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, byte[]>
      kafkaListenerByteArrayContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, byte[]> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(byteArrayConsumerFactory());
    return factory;
  }
}
