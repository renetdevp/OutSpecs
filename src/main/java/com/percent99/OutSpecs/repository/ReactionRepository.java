package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Reaction;
import com.percent99.OutSpecs.entity.ReactionType;
import com.percent99.OutSpecs.entity.TargetType;
import com.percent99.OutSpecs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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


}
