package com.project.apsas.integration.jubge;

import com.project.apsas.integration.brevo.BrevoFeignConfig;
import com.project.apsas.integration.jubge.dto.Laguageitem;
import com.project.apsas.integration.jubge.dto.LanguageResponse;
import com.project.apsas.integration.jubge.dto.SubmissionRCERequest;
import com.project.apsas.integration.jubge.dto.SubmissionRCEResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/languages/{id}")
    LanguageResponse getLanguageDetails(@PathVariable("id") int languageId);

    @GetMapping("/languages")
    List<Laguageitem> getLanguages();


}
