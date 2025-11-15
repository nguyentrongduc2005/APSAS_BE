package com.project.apsas.service.impl;

import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.request.content.CreateContentRequest;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.content.CreateContentResponse;
import com.project.apsas.entity.Content;
import com.project.apsas.entity.Tutorial;
import com.project.apsas.enums.ContentStatus;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.ContentRepository;
import com.project.apsas.repository.TutorialRepository;
import com.project.apsas.service.ContentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContentServiceImpl implements ContentService {

    Parser markdownParser;
    HtmlRenderer htmlRenderer;

    ContentRepository contentRepository;
    TutorialRepository tutorialRepository;

    @Override
    public CreateContentResponse createContent(Long tutorialId, CreateContentRequest request) {

        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));

        // 2. Logic chuyển đổi Markdown sang HTML
        String markdownInput = request.getBodyMd();
        Node document = markdownParser.parse(markdownInput);
        String htmlOutput = htmlRenderer.render(document);

        // 3. Xây dựng Entity
        Content newContent = Content.builder()
                .tutorialId(tutorialId)
                .title(request.getTitle())
                .bodyMd(markdownInput)
                .bodyHtmlCached(htmlOutput) // Lưu HTML đã chuyển đổi
                .orderNo(request.getOrderNo())
                .status(ContentStatus.valueOf(tutorial.getStatus().name()))
                .build();

        // 4. Lưu vào CSDL
        Content savedContent = contentRepository.save(newContent);

        // 5. Ánh xạ (Map) sang DTO Response
        return CreateContentResponse.builder()
                .id(savedContent.getId())
                .tutorialId(savedContent.getTutorialId())
                .title(savedContent.getTitle())
                .bodyMd(savedContent.getBodyMd())
                .bodyHtmlCached(savedContent.getBodyHtmlCached())
                .orderNo(savedContent.getOrderNo())
                .status(savedContent.getStatus())
                .build();
    }


}
