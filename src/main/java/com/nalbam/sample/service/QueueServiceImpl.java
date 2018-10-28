package com.nalbam.sample.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.nalbam.sample.domain.Queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class QueueServiceImpl implements QueueService {

    private String queueUrl;

    @Value("${aws.sqs.queueName}")
    private String queueName;

    @Value("${aws.sqs.maxTimeout}")
    private Integer maxTimeout;

    @Value("${aws.sqs.maxDelay}")
    private Integer maxDelay;

    @Autowired
    private AmazonSQS amazonSQS;

    @Async
    @Override
    public CompletableFuture<String> send(final Queue queue) {
        final String body = queue.toJson();
        Integer delay = queue.getDelay();
        if (delay < 0) {
            delay = 0;
        } else if (delay > this.maxDelay) {
            delay = this.maxDelay;
        }
        final SendMessageRequest req = new SendMessageRequest()
                .withQueueUrl(getQueueUrl())
                .withMessageBody(body)
                .withDelaySeconds(delay);
        return CompletableFuture.completedFuture(this.amazonSQS.sendMessage(req).getMessageId());
    }

    @Async
    @Override
    public CompletableFuture<List<Message>> receive() {
        final ReceiveMessageRequest req = new ReceiveMessageRequest()
                .withMaxNumberOfMessages(10)
                .withQueueUrl(getQueueUrl());
        return CompletableFuture.completedFuture(this.amazonSQS.receiveMessage(req).getMessages());
    }

    @Async
    @Override
    public void changeVisibility(final String handle, Integer timeout) {
        if (timeout < 0) {
            timeout = 0;
        } else if (timeout > this.maxTimeout) {
            timeout = this.maxTimeout;
        }
        this.amazonSQS.changeMessageVisibility(getQueueUrl(), handle, timeout);
    }

    @Async
    @Override
    public void delete(final String handle) {
        this.amazonSQS.deleteMessage(getQueueUrl(), handle);
    }

    private String getQueueUrl() {
        if (this.queueUrl != null) {
            return this.queueUrl;
        }
        try {
            this.amazonSQS.createQueue(this.queueName);
        } catch (final Exception e) {
            log.error("Queue createQueue [{}] {}", this.queueName, e.toString());
        }
        this.queueUrl = this.amazonSQS.getQueueUrl(this.queueName).getQueueUrl();
        return this.queueUrl;
    }

}
