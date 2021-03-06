package com.cuetodev.EJKA1.Controller;

import com.cuetodev.EJKA1.Sender.KafkaMessageProducer;
import com.cuetodev.EJKA1.Test.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaTestController {
    @Autowired
    KafkaMessageProducer kafkaMessageProducer;

    @PostMapping("/add/{topic}")
    public void addIdCustomer(@PathVariable String topic, @RequestBody String body)
    {
        kafkaMessageProducer.sendMessage(topic,body);
    }

    @PostMapping("/test")
    public void addTest() {
        Test test = new Test("David", 21, true);
        kafkaMessageProducer.sendObject("testing", test);
    }
}
