package com.project.apsas.service.impl;

import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.request.content.CreateContentRequest;
import com.project.apsas.dto.response.UploadResult;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.content.CreateContentResponse;
import com.project.apsas.entity.Content;
import com.project.apsas.entity.Media;
import com.project.apsas.entity.Tutorial;
import com.project.apsas.enums.ContentStatus;
import com.project.apsas.enums.MediaType;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.ContentRepository;
import com.project.apsas.repository.MediaRepository;
import com.project.apsas.repository.TutorialRepository;
import com.project.apsas.service.CloudinaryService;
import com.project.apsas.service.ContentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContentServiceImpl implements ContentService {

    Parser markdownParser;
    HtmlRenderer htmlRenderer;

    ContentRepository contentRepository;
    TutorialRepository tutorialRepository;
    CloudinaryService cloudinaryService;
    MediaRepository mediaRepository;

    @NonFinal
    @Value("${cloudinary.option.folder-name}")
    private String folder;

    @Override
    public CreateContentResponse createContent(Long tutorialId, CreateContentRequest request, List<MultipartFile> files) {

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

        Set<Media> mediaList = files.stream()
                .flatMap(multipartFile -> {

                    // 1. Kiểm tra loại file (ảnh/video)
                    MediaType mediaType = MediaType.fromImageOrVideoFile(multipartFile);

                    // 2. NẾU KHÔNG HỢP LỆ (null) -> BỎ QUA
                    if (mediaType == null) {
                        return Stream.empty(); // Bỏ qua file này
                    }

                    // 3. NẾU HỢP LỆ -> Tạo public_id VÀ UPLOAD
                    try {
                        // 3a. TẠO PUBLIC_ID DUY NHẤT
                        String publicId = UUID.randomUUID().toString();

                        // 3b. GỌI SERVICE UPLOAD
                        // (Giả sử service của bạn trả về UploadResult của Cloudinary)
                        UploadResult uploadResult = cloudinaryService.upload(
                                multipartFile,
                                folder,
                                publicId
                        );

                        // 3c. Lấy URL an toàn (https)
                        String fileUrl = uploadResult.getUrl();

                        // 3d. Xây dựng đối tượng Media
                        Media media = Media.builder()
                                .url(fileUrl) // URL thật từ Cloudinary
                                .contentId(savedContent.getId())
                                .caption("")
                                .orderNo(savedContent.getOrderNo())
                                .type(mediaType) // Enum IMAGE hoặc VIDEO
                                .build();

                        return Stream.of(media); // Trả về stream chứa 1 media

                    } catch (Exception e) {
                        // Xử lý lỗi nếu upload thất bại
                        return Stream.empty(); // Bỏ qua file bị lỗi
                    }
                }).collect(Collectors.toSet());
        if (!mediaList.isEmpty()) {
            mediaRepository.saveAll(mediaList);
        }

        long totalImages = mediaList.stream()
                .filter(media -> media.getType() == MediaType.IMAGE)
                .count();

        long totalVideos = mediaList.stream()
                .filter(media -> media.getType() == MediaType.VIDEO)
                .count();
        // 5. Ánh xạ (Map) sang DTO Response
        return CreateContentResponse.builder()
                .id(savedContent.getId())
                .totalImage((int)(totalImages))
                .totalVideo((int)(totalVideos))
                .tutorialId(savedContent.getTutorialId())
                .title(savedContent.getTitle())
                .bodyMd(savedContent.getBodyMd())
                .bodyHtmlCached(savedContent.getBodyHtmlCached())
                .orderNo(savedContent.getOrderNo())
                .status(savedContent.getStatus())
                .build();
    }


}
