package com.project.apsas.service.impl;

import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;
import com.project.apsas.entity.Tutorial;
import com.project.apsas.entity.User;
import com.project.apsas.enums.TutorialStatus;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.TutorialRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
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
    AuthService authService;
    TutorialRepository tutorialRepository;
    UserRepository userRepository;

    @Override
    public CreateTutorialResponse createTutorial(CreateTutorialRequest request) {

        Long userId = Long.parseLong(authService.currentId());
        User user = userRepository.findById(userId).orElseThrow(() ->
                new AppException(ErrorCode.NOT_FOUND)
        );

        Tutorial tutorial = Tutorial.builder()
                .createdBy(userId)
                .title(request.getTitle())
                .status(TutorialStatus.DRAFT)
                .summary(request.getSummary())
                .build();

        Tutorial result = tutorialRepository.save(tutorial);

        return CreateTutorialResponse.builder()
                .id(result.getId())
                .title(result.getTitle())
                .summary(result.getSummary())
                .createdBy(result.getCreatedBy())
                .status(result.getStatus())
                .build();
    }
}
