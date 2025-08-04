package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Reaction;
import com.percent99.OutSpecs.entity.ReactionType;
import com.percent99.OutSpecs.entity.TargetType;
import com.percent99.OutSpecs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    /**
     * User가 해당 target에 해당 반응 이미 했는지 체크
     */
    boolean existsByUserAndTargetTypeAndTargetIdAndReactionType(User user, TargetType targetType, Long targetId, ReactionType reactionType);

    /**
     * 해당 target에 해당 user가 한 어떠한 반응에 대한 삭제
     */
    void deleteByUserAndTargetTypeAndTargetIdAndReactionType(User user, TargetType targetType, Long targetId, ReactionType reactionType);

    /**
     * 해당 target에 대한 어떠한 반응의 수
     */
    long countByTargetTypeAndTargetIdAndReactionType(TargetType targetType, Long targetId, ReactionType reactionType);

    /**
     * User가 follow한 targetId 목록 찾기
     * @param user
     * @return User가 follow한 targetId 목록
     */
    @Query("SELECT r.targetId FROM Reaction r WHERE r.user = :user AND r.reactionType = 'FOLLOW' AND r.targetType = 'USER'")
    List<Long> findFollowedUserIds(@Param("user") User user);


    /**
     * User가 북마크한 Post targetId 목록 찾기
     * @param user
     * @return User가 북마크한 Post targetId 목록
     */
    @Query("SELECT r.targetId FROM Reaction r WHERE r.user = :user AND r.reactionType = 'BOOKMARK' AND r.targetType = 'POST'")
    List<Long> findBookmarkedPostIdsByUser(@Param("user") User user);
}
