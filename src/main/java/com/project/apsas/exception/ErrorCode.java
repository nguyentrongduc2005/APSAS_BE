package com.project.apsas.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAD_REQUEST("E4000", HttpStatus.BAD_REQUEST, "Request không hợp lệ."),
    VALIDATION_FAILED("E4001", HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ."),
    UNAUTHORIZED("E4010", HttpStatus.UNAUTHORIZED, "Chưa đăng nhập."),
    UNAUTHENTICATED("E4010", HttpStatus.UNAUTHORIZED, "đăng nhập không thành công"),
    FORBIDDEN("E4030", HttpStatus.FORBIDDEN, "Không đủ quyền."),
    NOT_FOUND("E4040", HttpStatus.NOT_FOUND, "Không tìm thấy."),
    CONFLICT("E4090", HttpStatus.CONFLICT, "Xung đột dữ liệu."),
    DUPLICATE("E4091", HttpStatus.CONFLICT, "Dữ liệu đã tồn tại."),
    UNSUPPORTED_MEDIA("E4150", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type không hỗ trợ."),
    INTERNAL_ERROR("E5000", HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống."),
    ACCESS_DENIED("E5001", HttpStatus.UNAUTHORIZED, "không có quyền truy cập" ),
    SUBMISSION_NOT_FOUND("E5002",HttpStatus.NOT_FOUND,"không tìm thấy submission"),
    USER_ESIXSTED("E5003", HttpStatus.BAD_REQUEST, "user đã tồn tại"),
    PASSWORD_INVALID("E5004", HttpStatus.BAD_REQUEST, "password invalid"),
    TOKEN_EXPIRED("E5005", HttpStatus.UNAUTHORIZED, "token hết hạn"),
    REFRESH_TOKEN_INVALID("E5006", HttpStatus.UNAUTHORIZED, "refresh token không hợp lệ"),
    REFRESH_TOKEN_NOT_FOUND("E5007", HttpStatus.UNAUTHORIZED, "không tìm thấy refresh token"),
    USER_NOT_FOUND("E5008", HttpStatus.NOT_FOUND, "không tìm thấy user"),
    DATE_INVALID("E5009", HttpStatus.BAD_REQUEST, "ngày không hợp lệ"),
    ASSIGNMENT_NOT_FOUND("E5010", HttpStatus.NOT_FOUND, "kh tìm thấy assigment"),
    ASSIGNMENT_NOT_OPEN("E5011", HttpStatus.BAD_REQUEST, "bài tập chưa mở"),
    ASSIGNMENT_HAVE_CLOSE("E5012", HttpStatus.BAD_REQUEST, "bài tập đã đống"),
    TUTORIAL_NOT_EXISTED("E5013", HttpStatus.BAD_REQUEST, "tutorial không tồn tại"),
    CONTENT_NOT_EXISTED("E5014", HttpStatus.BAD_REQUEST, "content không tồn tại"),
    ASSIGNMENT_NOT_EXISTED("E5015", HttpStatus.BAD_REQUEST, "assignment không tồn tại"),
    LIMITED("E5016", HttpStatus.BAD_REQUEST, "đã đến giới hạn"),
    USER_NOT_EXISTED("E5017", HttpStatus.NOT_FOUND, "không tìm thấy người dùng"),
    ROLE_NOT_FOUND("E5018", HttpStatus.NOT_FOUND, "không tìm thấy role"),
    CANNOT_DELETE_LAST_ADMIN("E5019", HttpStatus.BAD_REQUEST, "không thể xóa admin cuối cùng"),
    TIME_INVALID("E5020", HttpStatus.BAD_REQUEST, "thời gian không hợp lệ"),
    ROLE_EXISTED("E5021", HttpStatus.CONFLICT, "vai trò đã tồn tại"),
    PERMISSION_NOT_FOUND("E5022", HttpStatus.NOT_FOUND, "không tìm thấy permission"),
    ROLE_IN_USE("E5023", HttpStatus.BAD_REQUEST, "vai trò đang được sử dụng, không thể xóa")

    ;

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;



}
