package com.project.apsas.service.impl;

import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;
import com.project.apsas.service.TutorialService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TutorialServiceImpl implements TutorialService {


    @Override
    public CreateTutorialResponse createTutorial(CreateTutorialRequest request) {
        return null;
    }
}
