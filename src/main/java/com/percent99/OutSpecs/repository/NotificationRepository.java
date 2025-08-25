package com.percent99.OutSpecs.repository;

import com.percent99.OutSpecs.entity.Notification;
import com.percent99.OutSpecs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 받는 사람 기준으로 알림 찾기
     * @param receiver 받는 사람
     * @return 알림값
     */
    List<Notification> findByReceiverId(User receiver);
}
