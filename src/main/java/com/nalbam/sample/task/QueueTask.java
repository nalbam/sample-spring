package com.nalbam.sample.task;

import com.amazonaws.services.sqs.model.Message;
import com.google.gson.Gson;
import com.nalbam.sample.domain.Queue;
import com.nalbam.sample.service.QueueService;
import com.nalbam.sample.service.SendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class QueueTask {

    @Value("${aws.region}")
    private String region;

    @Autowired
    private QueueService queueService;

    @Autowired
    private SendService sendService;

    @Scheduled(fixedRate = 10000)
    public void counter_up() {
        Map<String, String> data = new HashMap<>();
        data.put("url", "http://sample-node:3000/counter/up");

        Queue queue = new Queue();
        queue.setType('2');
        queue.setDelay(0);
        queue.setData(data);
        queue.setTokens(new ArrayList<>());
        queue.setRegistered(new Date());

        this.queueService.send(queue)
                .exceptionally(e -> {
                    log.error("Queue send : {}", e.getMessage());
                    return null;
                })
                .thenApply(r -> {
                    //log.info("Queue send : {}", r.size());
                    return r;
                });
    }

    @Scheduled(fixedRate = 1000)
    public void receive() {
        this.queueService.receive()
                .exceptionally(e -> {
                    log.error("Queue receive : {}", e.getMessage());
                    return null;
                })
                .thenApply(r -> {
                    //log.info("Queue receive : {}", r.size());
                    receive(r);
                    return r;
                });
    }

    private void receive(final List<Message> list) {
        if (list.size() > 0) {
            log.info("Queue receive : {}", list.size());

            Queue queue;
            Long delay;

            for (final Message message : list) {
                queue = new Gson().fromJson(message.getBody(), Queue.class);

                log.info("Queue receive : {}", message.getMessageId());

                try {
                    // 발송시간 = 예약시간 + delay
                    delay = (queue.getDelay() * 1000) - new Date().getTime();

                    if (queue.getReserved() != null) {
                        delay = delay + queue.getReserved().getTime();
                    } else {
                        delay = delay + queue.getRegistered().getTime();
                    }

                    log.info("Queue receive : [{}] [delay:{}]", queue.getData(), milToSec(delay));

                    if (delay > 1000) {
                        // 연기
                        this.queueService.changeVisibility(message.getReceiptHandle(), milToSec(delay));
                        continue;
                    }

                    // 발송
                    this.sendService.send(queue);
                } catch (Exception e) {
                    log.error("Queue receive : {}", e.getMessage());
                }

                // 삭제
                this.queueService.delete(message.getReceiptHandle());
            }
        }
    }

    private Integer milToSec(final Long mil) {
        return (mil.intValue() / 1000);
    }

}
