package com.project.apsas.integration.jubge;

import com.project.apsas.integration.brevo.BrevoFeignConfig;
import com.project.apsas.integration.jubge.dto.SubmissionRCERequest;
import com.project.apsas.integration.jubge.dto.SubmissionRCEResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "jubge-api",
        url = "${jubge.base-url.base-url}"
)
public interface JubgeApiClient {
    @PostMapping("/submissions")
    SubmissionRCEResponse createAndRunSubmission(
            @RequestBody SubmissionRCERequest submissionRequest,
            @RequestParam("base64_encoded") boolean base64Encoded,
            @RequestParam("wait") boolean wait,
            @RequestParam("fields") String fields
    );

}
