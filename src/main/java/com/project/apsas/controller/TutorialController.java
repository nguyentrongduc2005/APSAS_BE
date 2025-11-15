package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/tutorials")
public class TutorialController {


    @PostMapping("/create")
    public ApiResponse<CreateTutorialResponse>  createTutorial(@RequestBody CreateTutorialRequest request){

        return null;
    }
}
