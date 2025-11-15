package com.project.apsas.service;

import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;

public interface TutorialService {
    public CreateTutorialResponse createTutorial(CreateTutorialRequest request);
}
