package com.project.apsas.service;

import com.project.apsas.dto.response.CodeFeedbackDTO;

import java.util.concurrent.CompletableFuture;

public interface AIFeedbackService {
    public CodeFeedbackDTO review(String code,String language , String statement_md);
    public CompletableFuture<CodeFeedbackDTO> reviewAsync(String code,String language , String statement_md);
}
