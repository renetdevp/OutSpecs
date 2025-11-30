package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.dto.ChatRoomResponseDTO;
import com.percent99.OutSpecs.entity.*;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {
  private final EntityManager entityManager;
  private final JPAQueryFactory queryFactory;
  private static final QChatRoom qChatRoom = QChatRoom.chatRoom;

  @Override
  public Optional<ChatRoom> searchChatRoomWithUsers(Long chatRoomId, Long userId) {
    QUser user1 = new QUser("user1");
    QUser user2 = new QUser("user2");

    return queryFactory.selectFrom(qChatRoom)
            .leftJoin(qChatRoom.user1, user1).fetchJoin()
            .leftJoin(qChatRoom.user2, user2).fetchJoin()
            .where(
                    qChatRoom.id.eq(chatRoomId)
            )
            .fetch().stream().distinct().findFirst();
  }

  @Override
  public boolean existsByIdAndUserId(Long chatRoomId, Long userId) {
    return queryFactory.from(qChatRoom)
            .where(
                    qChatRoom.id.eq(chatRoomId),
                    qChatRoom.user1.id.eq(userId)
                            .or(qChatRoom.user2.id.eq(userId))
            )
            .fetchFirst() != null;
  }

  @Override
  public boolean existsByUsersId(Long userId, Long targetId){
    return queryFactory.selectFrom(qChatRoom)
            .where(isParticipants(userId, targetId))
            .fetchFirst() != null;
  }

  @Override
  public ChatRoom findByUsersId(Long userId, Long targetId){
    return queryFactory.selectFrom(qChatRoom)
            .where(isParticipants(userId, targetId))
            .fetchFirst();
  }

  @Override
  public List<ChatRoomResponseDTO> findAllChatRoomWithDetails(Long userId) {
    QChatMessage qChatMessage = QChatMessage.chatMessage;
    QUser user1 = new QUser("user1");
    QUser user2 = new QUser("user2");
    QProfile profile1 = new QProfile("profile1");
    QProfile profile2 = new QProfile("profile2");
    QChatMessage latestId = new QChatMessage("latestId");

    JPAQuery<Long> maxIdSubquery = new JPAQuery<Long>(entityManager)
            .select(latestId.id.max())
            .from(latestId)
            .where(latestId.chatRoom.eq(qChatRoom))
            .groupBy(latestId.chatRoom);

    return queryFactory.select(
                    Projections.constructor(
                            ChatRoomResponseDTO.class,
                            qChatRoom,
                            qChatMessage,
                            profile1,
                            profile2
                    ))
            .from(qChatRoom)
            .where(qChatRoom.user1.id.eq(userId)
                    .or(qChatRoom.user2.id.eq(userId)))
            .leftJoin(qChatRoom.user1, user1).fetchJoin()
            .leftJoin(qChatRoom.user2, user2).fetchJoin()
            .leftJoin(qChatRoom.user1.profile, profile1).fetchJoin()
            .leftJoin(qChatRoom.user2.profile, profile2).fetchJoin()
            .leftJoin(qChatMessage).on(qChatMessage.id.eq(maxIdSubquery))
            .fetch();
  }

  @Override
  public ChatRoomResponseDTO findByIdWithDetails(Long chatRoomId) {
    QChatMessage qChatMessage = QChatMessage.chatMessage;
    QUser user1 = new QUser("user1");
    QUser user2 = new QUser("user2");
    QProfile profile1 = new QProfile("profile1");
    QProfile profile2 = new QProfile("profile2");

    return queryFactory.select(
            Projections.constructor(
                    ChatRoomResponseDTO.class,
                    qChatRoom,
                    qChatMessage,
                    profile1,
                    profile2
            ))
            .from(qChatRoom)
            .where(qChatRoom.id.eq(chatRoomId))
            .leftJoin(qChatRoom.user1, user1).fetchJoin()
            .leftJoin(qChatRoom.user2, user2).fetchJoin()
            .leftJoin(qChatRoom.user1.profile, profile1).fetchJoin()
            .leftJoin(qChatRoom.user2.profile, profile2).fetchJoin()
            .leftJoin(qChatMessage).on(
                    qChatMessage.id.eq(qChatRoom.lastMessageId))
            .fetchFirst();
  }

  private BooleanExpression isParticipants(Long userId, Long targetId){
    return (qChatRoom.user1.id.eq(userId)
                    .and(qChatRoom.user2.id.eq(targetId)))
            .or(qChatRoom.user1.id.eq(targetId)
                    .and(qChatRoom.user2.id.eq(userId))
            );
  }
}
