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

    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired
    private SendService sendService;

    @Scheduled(fixedRate = 3000)
    public void call_sample_node() {
        call("http://sample-node-" + profile + "/counter/down");
    }

    @Scheduled(fixedRate = 7000)
    public void call_sample_spring() {
        call("http://sample-spring-" + profile + "/spring");
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
