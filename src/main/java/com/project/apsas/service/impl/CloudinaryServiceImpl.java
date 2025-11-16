package com.project.apsas.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.project.apsas.dto.response.UploadResult;

import com.project.apsas.service.CloudinaryService;
import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    Cloudinary cloudinary;

    @NonFinal
    @Value("${cloudinary.option.folder-name}")
    String defaultFolder;

    @NonFinal
    @Value("${cloudinary.option.resource-type}")
    String resourceType;


    public UploadResult upload(MultipartFile file, String folder, String publicId) throws IOException {
        Map<String, Object> options = new HashMap<>();
        if (folder != null && !folder.isEmpty()) {
            options.put("folder", folder);
        } else {
            options.put("folder", defaultFolder);
        }
        if (publicId != null && !publicId.isEmpty()) {
            options.put("public_id", publicId);
            options.put("unique_filename", false);
            options.put("overwrite", true);
        } else {
            options.put("use_filename", true);
            options.put("unique_filename", true);
        }

        options.put("resource_type", resourceType); // Luôn dùng resource_type từ config
        Map result =  cloudinary.uploader().upload(file.getBytes(), options);
        UploadResult uploadResult = UploadResult.builder()
                .url(result.get("secure_url").toString())
                .format(result.get("format").toString())
                .height((Integer) result.get("height"))
                .width((Integer) result.get("width"))
                .publicId(result.get("public_id").toString())
                .resourceType(result.get("resource_type").toString())
                .build();
        return uploadResult;
    }

    public String getResizedUrl(String publicId, int width, int height) {
        return cloudinary.url()
//                .resourceType(this.resourceType)
                .transformation(new Transformation()
                        .width(width)
                        .height(height)
                        .crop("fill") // Kiểu crop: lấp đầy
                        .gravity("auto"))
                .secure(true)
                .generate(publicId);
    }

    public String getAvatarThumbnailUrl(String publicId) {
        return cloudinary.url()
                .transformation(new Transformation()
                        .width(50)
                        .height(50)
                        .crop("fill")
                        .gravity("face") // Ưu tiên crop vào khuôn mặt
                        .radius("max")) // Bo tròn tối đa
                .secure(true)
                .generate(publicId);
    }

    public Map delete(String publicId, String resourceType) throws IOException {
        if (resourceType == null || !List.of("image", "video", "raw").contains(resourceType)) {
            throw new IllegalArgumentException("Resource type không hợp lệ");
        }

        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", resourceType);

        return cloudinary.uploader().destroy(publicId, options);
    }

    public Map deleteFiles(List<String> publicIds) throws Exception {
        if (publicIds == null || publicIds.isEmpty()) {
            // Không có gì để xóa, trả về 1 Map rỗng
            return new HashMap();
        }

        Map<String, Object> options = new HashMap<>();

        // 1. Dùng 'resourceType' mặc định của class,
        //    giống hệt như hàm 'upload' của bạn
        options.put("resource_type", this.resourceType);

        // 2. Tùy chọn: Xóa cache CDN ngay lập tức (nên dùng)
        options.put("invalidate", true);

        // 3. Gọi API 'delete_resources' (xóa hàng loạt)
        //    Lưu ý: dùng .api() chứ không phải .uploader()
        return cloudinary.api().deleteResources(publicIds, options);
    }
}
