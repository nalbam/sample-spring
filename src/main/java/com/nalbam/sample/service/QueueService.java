package com.nalbam.sample.service;

import com.amazonaws.services.sqs.model.Message;
import com.nalbam.sample.domain.Queue;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface QueueService {

    CompletableFuture<String> send(Queue queue);

    CompletableFuture<List<Message>> receive();

    void changeVisibility(String handle, Integer timeout);

    void delete(String handle);

}
