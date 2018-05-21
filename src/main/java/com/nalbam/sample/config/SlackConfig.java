package com.nalbam.sample.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "slack")
public class SlackConfig {

    private String webhook = "https://hooks.slack.com/services/web/hook/token";

    private String channel = "sandbox";

    private String message = "Start";

}
