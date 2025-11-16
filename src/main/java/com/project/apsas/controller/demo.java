package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.event.FeedbackEvent;
import com.project.apsas.dto.event.SendMailEvent;
import com.project.apsas.dto.event.SubmitCodeEvent;
import com.project.apsas.dto.request.FeedbackRequest;
import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.dto.response.UploadResult;
import com.project.apsas.entity.Permission;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.Submission;
import com.project.apsas.entity.User;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.integration.kafka.ai.KafkaFeedbackProvider;
import com.project.apsas.integration.kafka.jubge.KafkaRCEProducer;
import com.project.apsas.integration.kafka.mail.KafkaMailProducer;
import com.project.apsas.repository.PermissionRepository;
import com.project.apsas.repository.RoleRepository;
import com.project.apsas.repository.SubmissionRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AIFeedbackService;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.CloudinaryService;
import com.project.apsas.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Properties;

@RestController
public class demo {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PermissionRepository permissionRepository;
    @Autowired
    AuthService authService;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    MailService mailService;

    @Autowired
    KafkaMailProducer kafkaMailProducer;

    @Autowired
    AIFeedbackService aiFeedbackService;

    @Autowired
    KafkaFeedbackProvider kafkaFeedbackProvider;
    @Autowired
    SubmissionRepository  submissionRepository;

    @Autowired
    KafkaRCEProducer kafkaRCEProducer;

    @PostMapping("/test")
    public String test(@RequestBody SubmitCodeEvent submitCodeEvent) {
        try {
            // In ra để kiểm tra xem đã nhận đúng dữ liệu chưa
            System.out.println("Đã nhận sự kiện cho submissionId: " + submitCodeEvent.getSubmissionId());
            System.out.println("Ngôn ngữ ID: " + submitCodeEvent.getLanguageId());

            // **Logic test chính của bạn:**
            // Giả sử producer có phương thức .send(event)
            kafkaRCEProducer.push("topic-execute",submitCodeEvent.getSubmissionId().toString(),submitCodeEvent);

            System.out.println("Đã gửi sự kiện tới Kafka thành công.");

            // Trả về 200 OK nếu thành công
            return "Đã gửi thành công sự kiện cho submissionId: " + submitCodeEvent.getSubmissionId();

        } catch (Exception e) {
            // Nếu Kafka bị lỗi hoặc có vấn đề
            System.err.println("Lỗi khi gửi sự kiện Kafka: " + e.getMessage());

            return "lỗi";
        }


    }





    @GetMapping("/hello")
    public String hello() {
        System.out.println(UserStatus.ACTIVE.name());
        Permission permission = Permission.builder()
                .name("update12")
                .description("update")
                .build();

        Role role = Role.builder()
                .name("admin12")
                .description("admin")
                .permissions(new HashSet<>() {{add(permission);}})
                .build();

        User user = User.builder()
                .email("duc@gmail.com")
                .password("123")
                .name("sdfsdfsdfsdfsdfsdfsdf")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>() {{add(role);}})
                .build();
        userRepository.save(user);
        return "Hello World";
    }




    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        System.out.println(ErrorCode.NOT_FOUND.name());
         throw new AppException(ErrorCode.NOT_FOUND);
//        return authService.login(loginRequest);
    }

    @PostMapping("/file")
    public ResponseEntity<UploadResult> upload(@RequestParam("file") MultipartFile file) {
        try {
            UploadResult uploadResult = cloudinaryService.upload(file, null, "jelell");
            String url = (String) uploadResult.getUrl();
//            Map map = cloudinaryService.delete("uploads/general/jelell", "image");
//            String url_resized = cloudinaryService.getAvatarThumbnailUrl("uploads/general/jelell");
            return ResponseEntity.ok().body(uploadResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(500).body(null);
    }
    @PostMapping("/mail")
    public ResponseEntity<String> sendmail() {
        String recipientEmail = "duc@yopmail.com";
        String recipientName = "Nguyễn Trọng Đức";

        Properties  props = new Properties();
        props.setProperty("otp_code", "432000425"); // Mã OTP giả
        props.setProperty("action", "kiểm tra hệ thống"); // Hành động
        props.setProperty("expiry_minutes", "5");
        // Thời gian hết hạn

        SendMailEvent event =  SendMailEvent.builder()
                .toEmail(recipientEmail)
                .name(recipientName)
                .params(props)
                .build();
        kafkaMailProducer.push("topic-mail",event.getToEmail(), event);
        return ResponseEntity.ok().body("thành công");
    }


    @PostMapping("/submission")
    public ResponseEntity<Submission> feedback(@RequestBody Object a) {
        Long id =  1L;
        Submission submission =  submissionRepository.findById(id).orElse(null);
        return ResponseEntity.ok().body(submission);
    }

    @PostMapping("/ai")
    public CodeFeedbackDTO ai() {
        Long id =  1L;
        Submission submission =  submissionRepository.findById(id).orElse(null);
        submission.getAssignment();
        FeedbackEvent event = FeedbackEvent.builder()
                .statement_md(submission.getAssignment().getStatementMd())
                .code(submission.getCode())
                .language(submission.getLanguage())
                .submissionId(submission.getId())
                .build();
        kafkaFeedbackProvider.push("topic-ai",submission.getId().toString(),event);
        return null;
    }


}
