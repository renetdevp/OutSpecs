package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.dto.ChatMessageDTO;
import com.percent99.OutSpecs.entity.ChatMessage;
import com.percent99.OutSpecs.entity.QChatMessage;
import com.percent99.OutSpecs.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {
  private final EntityManager em;
  private final JPAQueryFactory queryFactory;
  private static final QChatMessage qChatMessage = QChatMessage.chatMessage;
  private static final QUser qUser = QUser.user;

  @Override
  public ChatMessage findByIdWithSender(Long chatMessageId){
    return queryFactory.selectFrom(qChatMessage)
            .leftJoin(qChatMessage.sender, qUser).fetchJoin()
            .where(chatMessageIdEq(chatMessageId))
            .fetchFirst();
  }

  @Override
  public List<ChatMessage> findAllByChatRoomId(Long chatRoomId) {
    return queryFactory.selectFrom(qChatMessage)
            .where(chatRoomIdEq(chatRoomId))
            .fetch();
  }

  @Override
  public boolean existsByIdAndUserId(Long chatMessageId, Long userId) {
    return queryFactory.selectFrom(qChatMessage)
            .where(qChatMessage.id.eq(chatMessageId),
                    qChatMessage.sender.id.eq(userId))
            .fetchFirst() != null;
  }

  @Override
  public List<ChatMessage> searchChatMessage(Long chatRoomId, Long userId, LocalDateTime cursor, Long limit) {
    return queryFactory.selectFrom(qChatMessage)
            .where(
                    chatRoomIdEq(chatRoomId),
                    qChatMessage.chatRoom.user1.id.eq(userId)
                                    .or(qChatMessage.chatRoom.user2.id.eq(userId)),
                    createdAtBefore(cursor)
            )
            .limit(limit)
            .orderBy(qChatMessage.createdAt.desc())
            .fetch();
  }

  @Override
  public List<ChatMessageDTO> searchChatMessageAsDTO(Long chatRoomId, LocalDateTime cursor, Long limit){
    return queryFactory.select(
            Projections.constructor(
                    ChatMessageDTO.class,
                    qChatMessage.sender.id,
                    qChatMessage.content,
                    qChatMessage.createdAt,
                    qChatMessage.chatRoom.id))
            .from(qChatMessage)
            .where(
                    chatRoomIdEq(chatRoomId),
                    createdAtBefore(cursor)
            )
            .limit(limit)
            .orderBy(qChatMessage.createdAt.desc())
            .fetch();
  }

  @Override
  public void deleteAllBySenderId(Long senderId) {
    queryFactory.delete(qChatMessage)
            .where(qChatMessage.sender.id.eq(senderId))
            .execute();
    // 데이터 정합성 유지를 위한 persistence context clear
    // cold miss가 발생하는 단점이 있음
    em.clear();
  }

  private BooleanExpression chatMessageIdEq(Long chatMessageId){
    if (chatMessageId == null) return null;
    return qChatMessage.id.eq(chatMessageId);
  }

  private BooleanExpression chatRoomIdEq(Long chatRoomId) {
    if (chatRoomId == null) return null;
    return qChatMessage.chatRoom.id.eq(chatRoomId);
  }

  private BooleanExpression createdAtBefore(LocalDateTime cursor){
    if (cursor == null) return null;
    return qChatMessage.createdAt.before(cursor);
  }
}
