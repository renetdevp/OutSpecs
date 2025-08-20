package com.percent99.OutSpecs.handler;

import com.percent99.OutSpecs.exception.HttpResponseProcessingException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 인자 (ex. 댓글 작성자가 아님, 허용되지 않은 이미지 타입 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "INVALID_ARGUMENT",
                "message", ex.getMessage()
        ));
    }

    // 엔티티 조회 실패 (존재하지 않는 유저/게시글 등)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "errorCode", "NOT_FOUND",
                "message", ex.getMessage()
        ));
    }

    // 엔티티 중복 (이미 존재하는 회원, 이미 신청한 사용자 등)
    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<?> handleEntityExists(EntityExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "errorCode", "ALREADY_EXISTS",
                "message", ex.getMessage()
        ));
    }

    // 잘못된 상태 (ex. 모집이 종료됨, 삭제 불가능 상태 등)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "errorCode", "INVALID_STATE",
                "message", ex.getMessage()
        ));
    }

    // IO 관련 (파일 업로드 실패 등)
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "errorCode", "FILE_ERROR",
                "message", "파일 처리 중 오류가 발생했습니다."
        ));
    }

    // HTTP 응답 처리 실패 (외부 API 통신 오류 등)
    @ExceptionHandler(HttpResponseProcessingException.class)
    public ResponseEntity<?> handleHttpResponseProcessing(HttpResponseProcessingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "errorCode", "HTTP_RESPONSE_ERROR",
                "message", "외부 서비스 응답 처리 중 오류가 발생했습니다."
        ));
    }

    // OAuth 인증 오류 (ex. 지원하지 않는 Provider, 정지된 계정 등)
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<?> handleOAuth2Auth(OAuth2AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "errorCode", "OAUTH2_AUTH_ERROR",
                "message", ex.getMessage()
        ));
    }

    // 엔티티가 null인경우
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointer(NullPointerException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "errorCode", "AUTH_REQUIRED",
                "message", "로그인이 필요합니다."
        ));
    }

    // 마지막 방어선 (예상치 못한 모든 예외)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex) {
        ex.printStackTrace(); // 실제 운영에서는 log.error() 사용 권장
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "errorCode", "INTERNAL_ERROR",
                "message", "알 수 없는 오류가 발생했습니다."
        ));
    }
}

