package com.nalbam.sample.repository;

import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackAttachment;
import in.ashwanthkumar.slack.webhook.SlackMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SlackRepositoryImpl implements SlackRepository {

    @Value("${slack.webhook}")
    private String webhook;

    @Value("${slack.channel}")
    private String channel;

    @Async
    @Override
    public void send(final SlackMessage message) {
        send(this.channel, message);
    }

    @Async
    @Override
    public void send(final String channel, final SlackMessage message) {
        try {
            new Slack(this.webhook).sendToChannel(channel).push(message);
            log.info("slack send : {}", message.toString());
        } catch (final Exception e) {
            log.info("slack send error : {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void send(final SlackAttachment attachment) {
        send(this.channel, attachment);
    }

    @Async
    @Override
    public void send(final String channel, final SlackAttachment attachment) {
        try {
            new Slack(this.webhook).sendToChannel(channel).push(attachment);
            log.info("slack send : {}", attachment.getText());
        } catch (final Exception e) {
            log.info("slack send error : {}", e.getMessage());
        }
    }

}
