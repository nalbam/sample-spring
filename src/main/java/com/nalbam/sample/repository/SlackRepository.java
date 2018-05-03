package com.nalbam.sample.repository;

import in.ashwanthkumar.slack.webhook.SlackAttachment;
import in.ashwanthkumar.slack.webhook.SlackMessage;

public interface SlackRepository {

    void send(SlackMessage message);

    void send(String channel, SlackMessage message);

    void send(SlackAttachment attachment);

    void send(String channel, SlackAttachment attachment);

}
