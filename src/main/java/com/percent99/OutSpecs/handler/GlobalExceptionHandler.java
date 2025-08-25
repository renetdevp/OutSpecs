package com.percent99.OutSpecs.handler;

import com.percent99.OutSpecs.exception.HttpResponseProcessingException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 잘못된 인자 (ex. 댓글 작성자가 아님, 허용되지 않은 이미지 타입 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model, HttpServletRequest request) {
        log.warn("요청 [{} {}] - 잘못된 인자: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    // 엔티티 조회 실패 (존재하지 않는 유저/게시글 등)
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFound(EntityNotFoundException ex, Model model, HttpServletRequest request) {
        log.warn("요청 [{} {}] - 엔티티 없음: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    // 엔티티 중복 (이미 존재하는 회원, 이미 신청한 사용자 등)
    @ExceptionHandler(EntityExistsException.class)
    public String handleEntityExists(EntityExistsException ex, Model model, HttpServletRequest request) {
        log.warn("요청 [{} {}] - 엔티티 중복: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    // 잘못된 상태 (모집이 종료됨, 삭제 불가능 상태 등)
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, Model model, HttpServletRequest request) {
        log.warn("요청 [{} {}] - 잘못된 상태: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    // IO 관련 (파일 업로드 실패 등)
    @ExceptionHandler(IOException.class)
    public String handleIOException(IOException ex, Model model, HttpServletRequest request) {
        log.error("요청 [{} {}] - 파일 처리 오류", request.getMethod(), request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "파일 처리 중 오류가 발생했습니다.");
        return "error/error";
    }

    // HTTP 응답 처리 실패 (외부 API 통신 오류 등)
    @ExceptionHandler(HttpResponseProcessingException.class)
    public String handleHttpResponseProcessing(HttpResponseProcessingException ex, Model model, HttpServletRequest request) {
        log.error("요청 [{} {}] - 외부 API 오류", request.getMethod(), request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "외부 서비스 응답 처리 중 오류가 발생했습니다.");
        return "error/error";
    }
    // OAuth 인증 오류 (ex. 지원하지 않는 Provider, 정지된 계정 등)
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public String handleOAuth2Auth(OAuth2AuthenticationException ex, Model model, HttpServletRequest request) {
        log.warn("요청 [{} {}] - OAuth 인증 오류: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    // 엔티티가 null인경우
    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointer(NullPointerException ex, Model model, HttpServletRequest request) {
        log.error("요청 [{} {}] - NullPointerException", request.getMethod(), request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "알 수 없는 오류가 발생했습니다.");
        return "error/error";
    }

    // 페이지 리소스 에러
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFound(NoResourceFoundException ex, Model model, HttpServletRequest request) {
        log.error("요청 [{} {}] - NoResourceFoundException", request.getMethod(), request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "요청하신 페이지를 찾을 수 없습니다. ");
        return "error/error";
    }

    // 예상치 못한 모든 예외
    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, Model model, HttpServletRequest request) {
        log.error("요청 [{} {}] - 알 수 없는 예외 발생", request.getMethod(), request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "예기치 않은 오류가 발생했습니다.");
        return "error/error";
    }
}

