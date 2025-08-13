package com.percent99.OutSpecs.entity;

/**
 * <ul><strong>게시글 타입</strong>
 *     <li>QNA : q&A 게시판</li>
 *     <li>TEAM : 팀 모집 게시판</li>
 *     <li>PLAY : 유저 추천 나가서 놀기 게시판</li>
 *     <li>AIPLAY : AI 추천 나가서 놀기 게시판</li>
 *     <li>FREE : 자유 게시판</li>
 *     <li>RECRUIT : 채용공고 게시판</li>
 * </ul>
 */
public enum PostType {
    QNA, TEAM, PLAY, AIPLAY, FREE, RECRUIT;

    public String pathPrefix(){
        return switch (this){
            case QNA -> "qna";
            case FREE -> "free";
            case TEAM -> "team";
            case AIPLAY -> "ai-play";
            case PLAY -> "play";
            case RECRUIT -> "recruit";
        };
    }
    public String displayName(){
        return switch (this){
            case QNA -> "Q&A게시판";
            case FREE -> "자유게시판";
            case TEAM -> "팀 모집";
            case AIPLAY -> "AI랑 나가서 놀기";
            case PLAY -> "나가서놀기";
            case RECRUIT -> "채용 공고";
        };
    }
}