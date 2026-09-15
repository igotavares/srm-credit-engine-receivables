package br.com.ibeans.receivables.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientException;

@Configuration
public class RetryTemplateConfig {

    @Bean
    public RetryTemplate retryTemplate() {
         return RetryTemplate.builder()
                 .maxAttempts(3)
                 .fixedBackoff(150)
                 .retryOn(RestClientException .class)
                 .build();
    }

}
