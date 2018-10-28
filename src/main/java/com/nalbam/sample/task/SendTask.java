package com.nalbam.sample.task;

import com.nalbam.sample.domain.Queue;
import com.nalbam.sample.service.SendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SendTask {

    @Autowired
    private SendService sendService;

    @Scheduled(fixedRate = 10000)
    public void call_sample_node() {
        call("http://sample-node:3000/counter/down");
    }

    @Scheduled(fixedRate = 10000)
    public void call_sample_spring() {
        call("http://sample-spring:8080/spring");
    }

    private void call(String url) {
        Map<String, String> data = new HashMap<>();
        data.put("url", url);

        Queue queue = new Queue();
        queue.setType('2');
        queue.setDelay(0);
        queue.setData(data);
        queue.setTokens(new ArrayList<>());
        queue.setRegistered(new Date());

        // 발송
        this.sendService.send(queue);
    }

}
