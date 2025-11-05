package com.project.apsas.integration.brevo;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

public class BrevoFeignClientInterceptor implements RequestInterceptor {


    private String apiKey;

    public BrevoFeignClientInterceptor( String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("api-key", apiKey);
        requestTemplate.header("accept", "application/json");
        requestTemplate.header("content-type", "application/json");
    }
}
