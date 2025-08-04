package com.percent99.OutSpecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 채팅방 정보를 담는 엔티티 <br>
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_chatbot", nullable = false)
    private Boolean isChatbot;

    @Column(name = "last_message_id")
    private Long lastMessageId;

}
