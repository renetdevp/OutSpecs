package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.CommentType;
import lombok.Getter;
import lombok.Setter;

/**
 * Comment 생성·수정 시 사용되는 데이터 전송 객체(DTO) *
 * <ul>
 *     <li>user : 댓글 작성자 User 엔티티</li>
 *     <li>type : 댓글 유형 (COMMENT, ANSWER, REPLY)</li>
 *     <li>parentId : 상위 댓글 또는 게시물의 ID</li>
 *     <li>content : 댓글 내용</li>
 * </ul>
 */
@Getter
@Setter
public class CommentDTO {

    private Long userId;
    private CommentType type;
    private Long parentId;
    private String content;
}