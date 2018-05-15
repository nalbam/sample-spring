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

    @Autowired
    private SlackRepository slackRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final Map<String, String> data = PackageUtil.getData(this.getClass());

        log.info("Context refreshed : [{}] [{}] [{}] [{}]", data.get("artifactId"), this.profile, data.get("version"), event.getTimestamp());

        final SlackMessage message = new SlackMessage("Context refreshed ")
                .code(data.get("artifactId")).text(" ").code(this.profile).text(" ").code(data.get("version"));
        this.slackRepository.send(message);
    }

}
