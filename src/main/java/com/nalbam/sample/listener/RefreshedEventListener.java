package com.nalbam.sample.listener;

import com.nalbam.sample.config.SlackConfig;
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

    private final SlackConfig slackConfig;

    private SlackRepository slackRepository;

    @Autowired
    public RefreshedEventListener(SlackRepository slackRepository, SlackConfig slackConfig) {
        this.slackRepository = slackRepository;
        this.slackConfig = slackConfig;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final Map<String, String> data = PackageUtil.getData(this.getClass());

        log.info("{} : [{}] [{}] [{}] [{}]", slackConfig.getMessage(), this.profile, data.get("artifactId"), data.get("version"), event.getTimestamp());

        this.slackRepository.send(
                new SlackMessage(slackConfig.getMessage()).text(" ")
                        .code(this.profile).text(" ")
                        .code(data.get("artifactId")).text(" ")
                        .code(data.get("version"))
        );
    }

}
