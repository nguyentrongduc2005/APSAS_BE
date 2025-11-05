package com.project.apsas.integration.brevo;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class BrevoFeignConfig {

    @Value("${brevo.api-key}")
    private String apiKey;

    @Bean BrevoFeignClientInterceptor brevoFeignClientInterceptor() {
        return new BrevoFeignClientInterceptor(apiKey);
    }
}
