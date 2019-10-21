package com.nalbam.sample.service;

import com.google.android.gcm.server.*;
import com.nalbam.sample.domain.Queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
class SendServiceImpl implements SendService {

    private final RestTemplate restTemplate;

    public SendServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Async
    @Override
    public void send(final Queue queue) {
        log.info("Send send [{}]", queue.getType());

        switch (queue.getType()) {
        case '1':
            sendPush(queue);
            break;
        case '2':
            sendCallback(queue);
            break;
        case '9':
            sendTest(queue);
            break;
        default:
            log.error("Not support type.");
        }
    }

    private void sendPush(final Queue queue) {
        final Map<String, Object> data = queue.getData();

        Notification notification = null;

        // Notification 생략 - 안드로이드
        if (!data.get("device").equals('1')) {
            notification = new Notification.Builder(data.get("icon").toString()).color(data.get("color").toString())
                    .title(data.get("title").toString()).body(data.get("body").toString()).build();
        }

        final Message message = new Message.Builder().priority(Message.Priority.HIGH).notification(notification)
                .addData("title", data.get("title").toString()).addData("body", data.get("body").toString()).build();

        // NotRegistered : 등록되지 않은 기기
        // MismatchSenderId : 일치하지 않는 발신자
        // InvalidRegistration : 잘못된 등록 토큰
        final List<String> errors = Arrays.asList("NotRegistered", "InvalidRegistration", "MismatchSenderId");

        try {
            final Sender sender = new Sender(queue.getKey());

            final MulticastResult multicastResult = sender.send(message, queue.getTokens(), 3);

            log.info("Send sendPush [device:{}] [total:{}] [success:{}] [failure:{}]", data.get("device"),
                    multicastResult.getTotal(), multicastResult.getSuccess(), multicastResult.getFailure());

            if (multicastResult.getFailure() == 0) {
                return;
            }

            log.warn("Send sendPush [device:{}] {} ", data.get("device"), multicastResult.toString());

            try {
                String token;
                Result result;

                for (int i = 0; i < queue.getTokens().size(); i++) {
                    token = queue.getTokens().get(i);
                    result = multicastResult.getResults().get(i);

                    if (result.getErrorCodeName() == null) {
                        // log.info("Send sendPush [{}]", r.getMessageId());
                        continue;
                    }

                    if (errors.contains(result.getErrorCodeName())) {
                        // TODO remove token
                        log.info("Send sendPush remove {}", token);
                    } else {
                        log.warn("Send sendPush {}", result);
                    }
                }
            } catch (final Exception e) {
                log.error("Send sendPush {}", e.toString());
            }
        } catch (final IOException e) {
            log.error("Send sendPush {}", e.getMessage());
            log.error("Send sendPush {}", message.toString());

            e.printStackTrace();
        }
    }

    private CompletableFuture<String> sendCallback(final Queue queue) {
        final String url;

        try {
            url = queue.getData().get("url").toString();
        } catch (final Exception e) {
            log.error("Send sendCallback : URI is null");
            return null;
        }

        log.info("Send sendCallback req : {}", url);

        String res = restTemplate.getForObject(url, String.class);

        log.info("Send sendCallback res : {}", res);

        return CompletableFuture.completedFuture(res);

        // toFuture(this.asyncRestTemplate.getForEntity(url, String.class))
        // .exceptionally(e -> {
        // log.error("Send sendCallback : {}", e.getMessage());
        // return null;
        // })
        // .thenApply(r -> {
        // log.info("Send sendCallback : {}", r.getStatusCode());
        // return r;
        // });
    }

    private void sendTest(final Queue queue) {
        log.info("Send sendTest [delay:{}]", queue.getDelay() / 1000);

        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    // private <T> CompletableFuture<T> toFuture(final ListenableFuture<T> lf) {
    // final CompletableFuture<T> cf = new CompletableFuture<>();
    // lf.addCallback(cf::complete, cf::completeExceptionally);
    // return cf;
    // }

}
