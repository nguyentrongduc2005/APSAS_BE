package com.project.apsas.integration.jubge;

import com.project.apsas.integration.brevo.BrevoFeignConfig;
import com.project.apsas.integration.jubge.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "jubge-api",
        url = "${jubge.base-url}"
)
public interface JubgeApiClient {
    @PostMapping("/submissions?wait=true")
    SubmissionRCEResponse createAndRunSubmission(
            @RequestBody SubmissionRCERequest submissionRequest,
            @RequestParam("base64_encoded") boolean base64Encoded,
            @RequestParam("fields") String fields
    );

    @PostMapping("/submissions/batch?wait=true")
    List<SubmissionToken> createBatchSubmissions(
            @RequestBody BatchSubmissionRequest batchRequest,
            @RequestParam("base64_encoded") boolean base64Encoded
    );


    @GetMapping("/submissions/batch")
    BatchSubmissionResult getBatchSubmissionResults(
            // Đây là query param: ?tokens=token1,token2,token3
            @RequestParam("tokens") String tokens,
            @RequestParam("base64_encoded") boolean base64Encoded,
            @RequestParam("fields") String fields
    );

    @GetMapping("/languages/{id}")
    LanguageResponse getLanguageDetails(@PathVariable("id") int languageId);

    @GetMapping("/languages")
    List<Laguageitem> getLanguages();



}
