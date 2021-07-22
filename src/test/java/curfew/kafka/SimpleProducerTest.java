package curfew.kafka;

import curfew.util.DefaultTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Created by manish.shukla on 2020/4/2. */
public class SimpleProducerTest extends DefaultTest {
  @Autowired SimpleProducer simpleProducer;

  @Test
  public void testPutObject() {
    simpleProducer.postObjectInTopic("test123", "payload");
  }

  @Test
  public void testPutObjectToDefaultQueue() {
    simpleProducer.postObjectInDefaultTopic("test123", "payload");
  }
}
