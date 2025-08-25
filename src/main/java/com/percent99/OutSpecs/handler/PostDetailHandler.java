package com.percent99.OutSpecs.handler;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostType;

/**
 * post 테이블 유형별 상세 정보 설정을 위한 strategy 패턴 인터페이스
 * <P>
 *   각 구현체는 특정 PostType을 지원하며,
 *   해당 타입의 상세 필드를 엔티티에 세팅한다.
 * </P>
 */
public interface PostDetailHandler {
    /**
     * 이 핸들러가 지원하는 PostType 인지 확인
     * @param type 게시글 유형
     * @return 해당 유형이면 true
     */
    boolean supports(PostType type);

    /**
     * 해당 유형(PostDTO)의 상세정보를 엔티티(Post)에 세팅한다.
     * @param post 대상 Post 엔티티
     * @param dto 요청으로 전달된 DTO
     */
    void handle(Post post, PostDTO dto);
}