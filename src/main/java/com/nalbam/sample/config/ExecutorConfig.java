package com.nalbam.bot.config;

import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLException;
import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class ExecutorConfig extends AsyncConfigurerSupport {

    @Value("${spring.application.name}")
    private String name;

    @Bean
    public RestTemplate restTemplate() throws SSLException {
        final Netty4ClientHttpRequestFactory nettyFactory = new Netty4ClientHttpRequestFactory();
        nettyFactory.setSslContext(SslContextBuilder.forClient().build());
        return new RestTemplate(nettyFactory);
    }

    @Bean
    public AsyncRestTemplate asyncRestTemplate() throws SSLException {
        final Netty4ClientHttpRequestFactory nettyFactory = new Netty4ClientHttpRequestFactory();
        nettyFactory.setSslContext(SslContextBuilder.forClient().build());
        return new AsyncRestTemplate(nettyFactory);
    }

    @Override
    public Executor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(9);
        executor.setMaxPoolSize(99);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix(this.name + "-");
        executor.initialize();
        return executor;
    }

}
