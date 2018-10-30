package com.nalbam.sample.task;

import com.nalbam.sample.domain.Queue;
import com.nalbam.sample.service.SendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class SendTask {

    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired
    private SendService sendService;

    @Scheduled(fixedRate = 3000)
    public void call_sample_node() {
        call("http://sample-node-" + profile + "/counter/up");
    }

    @Scheduled(fixedRate = 2000)
    public void call_sample_spring() {
        call("http://sample-spring-" + profile + "/spring");
    }

    @Scheduled(fixedRate = 5000)
    public void call_sample_stress() {
        call("http://sample-spring-" + profile + "/stress");
    }

    @Scheduled(fixedRate = 3000)
    public void call_sample_dealy() {
        call("http://sample-spring-" + profile + "/dealy/1");
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

        // send
        this.sendService.send(queue);
    }

}
