package com.project.apsas.integration.brevo;

import com.project.apsas.integration.brevo.dto.SendEmailRequest;
import com.project.apsas.integration.brevo.dto.SendEmailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "brevo-api",
        url = "${brevo.base-url}",
        configuration = BrevoFeignConfig.class
)
public interface BrevoApiClient {
    @PostMapping("/smtp/email")
    SendEmailResponse sendEmail(@RequestBody SendEmailRequest sendEmailRequest);
}
