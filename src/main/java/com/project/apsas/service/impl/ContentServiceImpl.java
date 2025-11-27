package com.project.apsas.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apsas.dto.mapping.ConfigJson;
import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.request.content.CreateContentRequest;
import com.project.apsas.dto.request.content.UpdateContentRequest;
import com.project.apsas.dto.response.UploadResult;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.content.CreateContentResponse;
import com.project.apsas.dto.response.content.UpdateContentResponse;
import com.project.apsas.dto.response.tutorial.DetailAssignmentResponse;
import com.project.apsas.dto.response.tutorial.DetailContentResponse;
import com.project.apsas.entity.*;
import com.project.apsas.enums.ContentStatus;
import com.project.apsas.enums.MediaType;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.mapper.MediaMapper;
import com.project.apsas.repository.AssignmentRepository;
import com.project.apsas.repository.ContentRepository;
import com.project.apsas.repository.MediaRepository;
import com.project.apsas.repository.TutorialRepository;
import com.project.apsas.service.AuthService;
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

import java.util.ArrayList;
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
    AssignmentRepository assignmentRepository;
    ContentRepository contentRepository;
    TutorialRepository tutorialRepository;
    CloudinaryService cloudinaryService;
    MediaRepository mediaRepository;
    ObjectMapper objectMapper;
    MediaMapper  mediaMapper;
    private final AuthService authService;

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
        long totalImages = 0;
        long totalVideos = 0;
        // 4. Lưu vào CSDL
        Content savedContent = contentRepository.save(newContent);
        if (files != null && !files.isEmpty()) {
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
                                    .pubicId(uploadResult.getPublicId())
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

            totalImages = mediaList.stream()
                    .filter(media -> media.getType() == MediaType.IMAGE)
                    .count();

            totalVideos = mediaList.stream()
                    .filter(media -> media.getType() == MediaType.VIDEO)
                    .count();
        }
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

    @Override
    @Transactional // Rất quan trọng!
    public UpdateContentResponse updateContent(Long contentId, UpdateContentRequest request, List<MultipartFile> filesAdd) {

        // 1. Lấy Content và Tutorial
        Content existingContent = contentRepository.findById(contentId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_EXISTED));

        Tutorial tutorial = tutorialRepository.findById(existingContent.getTutorialId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));

        // 2. CHECK BẢO MẬT (Theo yêu cầu của bạn)
        // Giả sử Tutorial có trường createdBy (username)
        Long currentUsername = Long.parseLong(authService.currentId());
        if (!tutorial.getCreatedBy().equals(currentUsername)) {
            throw new AppException(ErrorCode.FORBIDDEN); // Không có quyền sửa
        }

        // 3. XỬ LÝ XÓA MEDIA (filesDelete)
        if (request.getFilesDelete() != null && !request.getFilesDelete().isEmpty()) {
            List<Media> mediaToDelete = mediaRepository.findAllById(request.getFilesDelete());

            List<String> publicIdsToDelete = new ArrayList<>();

            for (Media media : mediaToDelete) {
                // Check xem media này có đúng là của content này không
                if (media.getContentId().equals(contentId)) {
                    publicIdsToDelete.add(media.getPubicId());
                }
            }

            // Xóa trên Cloudinary (giả sử service của bạn có thể xóa nhiều file)
            if (!publicIdsToDelete.isEmpty()) {
                try {
                    // Bạn cần tự implement hàm `deleteFiles` trong cloudinaryService
                    // Hoặc lặp và gọi delete từng file
                    cloudinaryService.deleteFiles(publicIdsToDelete);
                } catch (Exception e) {

                }
            }

            // Xóa khỏi DB
            mediaRepository.deleteAllInBatch(mediaToDelete);
        }

        // 4. XỬ LÝ THÊM MEDIA (filesAdd) - Giống hệt createContent
        if (filesAdd != null && !filesAdd.isEmpty()) {
            Set<Media> newMediaList = filesAdd.stream()
                    .flatMap(multipartFile -> {
                        MediaType mediaType = MediaType.fromImageOrVideoFile(multipartFile);
                        if (mediaType == null) return Stream.empty();
                        try {
                            String publicId = UUID.randomUUID().toString();
                            UploadResult uploadResult = cloudinaryService.upload(multipartFile, folder, publicId);

                            Media media = Media.builder()
                                    .url(uploadResult.getUrl())
                                    .pubicId(uploadResult.getPublicId()) // Phải lưu cái này
                                    .contentId(contentId)
                                    .type(mediaType)
                                    .orderNo(existingContent.getOrderNo()) // Lấy order của content
                                    .build();
                            return Stream.of(media);
                        } catch (Exception e) {

                            return Stream.empty();
                        }
                    }).collect(Collectors.toSet());

            mediaRepository.saveAll(newMediaList);
        }

        // 5. CẬP NHẬT TRƯỜNG CỦA CONTENT
        existingContent.setTitle(request.getTitle());
        existingContent.setOrderNo(request.getOrderNo());
        existingContent.setStatus(ContentStatus.valueOf(tutorial.getStatus().name()));

        // Nếu bodyMd có thay đổi -> render lại HTML
        if (request.getBodyMd() != null) {
            String markdownInput = request.getBodyMd();
            Node document = markdownParser.parse(markdownInput);
            String htmlOutput = htmlRenderer.render(document);
            existingContent.setBodyMd(markdownInput);
            existingContent.setBodyHtmlCached(htmlOutput);
        }

        // (Lưu lại, dù @Transactional tự lo, nhưng lưu cho rõ ràng)
        Content savedContent = contentRepository.save(existingContent);

        // 6. ĐẾM VÀ TRẢ VỀ RESPONSE
        Set<Media> finalMediaList = mediaRepository.findByContentId(contentId);

        long totalImages = finalMediaList.stream().filter(m -> m.getType() == MediaType.IMAGE).count();
        long totalVideos = finalMediaList.stream().filter(m -> m.getType() == MediaType.VIDEO).count();

        return UpdateContentResponse.builder()
                .id(savedContent.getId())
                .tutorialId(savedContent.getTutorialId())
                .title(savedContent.getTitle())
                .bodyMd(savedContent.getBodyMd())
                .bodyHtmlCached(savedContent.getBodyHtmlCached())
                .orderNo(savedContent.getOrderNo())
                .status(savedContent.getStatus())
                .totalImage((int) totalImages)
                .totalVideo((int) totalVideos)
                .build();
    }

    @Override
    public DetailContentResponse detailContentTutorial(Long contentId) {
        Content content = contentRepository.findById(contentId).orElseThrow(() ->
                new AppException(ErrorCode.CONTENT_NOT_EXISTED));

        List<Media> finalMediaList = content.getMediaList().stream().toList();
        if (content.getBodyHtmlCached() == null || content.getBodyHtmlCached().isEmpty()) {
            Node document = markdownParser.parse(content.getBodyMd());
            content.setBodyHtmlCached(htmlRenderer.render(document));
        }
        return DetailContentResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .bodyHtml(content.getBodyHtmlCached())
                .totalMedia(finalMediaList.size())
                .mediaList(mediaMapper.toMediaList(finalMediaList))
                .createdDate(content.getCreatedAt())
                .build();
    }


}
