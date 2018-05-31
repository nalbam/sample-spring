package com.nalbam.sample.listener;

import com.nalbam.sample.repository.SlackRepository;
import com.nalbam.sample.util.PackageUtil;
import in.ashwanthkumar.slack.webhook.SlackMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class RefreshedEventListener implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${app.profile}")
    private String profile;

    @Value("${message}")
    private String message;

    private final SlackRepository slackRepository;

    @Autowired
    public RefreshedEventListener(SlackRepository slackRepository) {
        this.slackRepository = slackRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final Map<String, String> data = PackageUtil.getData(this.getClass());

        log.info("{} : [{}] [{}] [{}] [{}]", this.message, this.profile, data.get("artifactId"), data.get("version"), event.getTimestamp());

        log.info("user.home : [{}]", System.getProperty("user.home"));

        this.slackRepository.send(
                new SlackMessage(this.message).text(" ")
                        .code(this.profile).text(" ")
                        .code(data.get("artifactId")).text(" ")
                        .code(data.get("version"))
        );
    }

}
