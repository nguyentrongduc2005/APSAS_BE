package com.project.apsas.service;

import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.request.tutorial.UpdateTutorialRequest;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;
import com.project.apsas.dto.response.tutorial.DetailTutorialResponse;

public interface TutorialService {
    public CreateTutorialResponse createTutorial(CreateTutorialRequest request);
    public Boolean updateTutorial(UpdateTutorialRequest request, Long tutorialId);

    public DetailTutorialResponse getTutorialDetail(Long tutorialId);
}
