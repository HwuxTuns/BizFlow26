package com.example.bizflow.controller;

import com.example.bizflow.entity.Message;
import com.example.bizflow.repository.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest request) {
        if (request == null || request.senderId == null || request.receiverId == null) {
            return ResponseEntity.badRequest().body("sender_id and receiver_id are required");
        }
        if (request.content == null || request.content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("content is required");
        }

        Message message = new Message();
        message.setSenderId(request.senderId);
        message.setReceiverId(request.receiverId);
        message.setSenderName(safeName(request.senderName));
        message.setReceiverName(safeName(request.receiverName));
        message.setContent(request.content.trim());
        message.setCreatedAt(LocalDateTime.now());

        Message saved = messageRepository.save(message);
        return ResponseEntity.ok(new MessageResponse(saved));
    }

    @GetMapping("/messages/{userId}/{ownerId}")
    public ResponseEntity<List<MessageResponse>> getThread(@PathVariable Long userId, @PathVariable Long ownerId) {
        List<Message> rows = messageRepository.findThread(userId, ownerId);
        List<MessageResponse> result = rows.stream().map(MessageResponse::new).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(@RequestParam Long ownerId) {
        List<Message> latestMessages = messageRepository.findConversationLatestMessages(ownerId);
        List<ConversationResponse> result = new ArrayList<>();

        for (Message message : latestMessages) {
            Long userId = ownerId.equals(message.getSenderId()) ? message.getReceiverId() : message.getSenderId();
            String userName = ownerId.equals(message.getSenderId())
                    ? safeName(message.getReceiverName())
                    : safeName(message.getSenderName());

            long unreadCount = messageRepository.countUnreadMessages(userId, ownerId);

            ConversationResponse item = new ConversationResponse();
            item.userId = userId;
            item.userName = userName;
            item.lastMessage = message.getContent();
            item.lastMessageAt = message.getCreatedAt();
            item.unreadCount = unreadCount;
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/messages/mark-as-read")
    public ResponseEntity<?> markMessagesAsRead(@RequestBody MarkAsReadRequest request) {
        if (request == null || request.senderId == null || request.receiverId == null) {
            return ResponseEntity.badRequest().body("senderId and receiverId are required");
        }

        int updated = messageRepository.markMessagesAsRead(request.senderId, request.receiverId);
        return ResponseEntity.ok(new Object() {
            public final int updated_count = updated;
            public final String status = "success";
        });
    }

    private String safeName(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Người dùng";
        }
        return value.trim();
    }

    public static class MessageRequest {
        public Long senderId;
        public Long receiverId;
        public String senderName;
        public String receiverName;
        public String content;
    }

    public static class MessageResponse {
        public Long id;
        public Long senderId;
        public Long receiverId;
        public String senderName;
        public String receiverName;
        public String content;
        public LocalDateTime createdAt;
        public Boolean isRead;

        public MessageResponse(Message message) {
            this.id = message.getId();
            this.senderId = message.getSenderId();
            this.receiverId = message.getReceiverId();
            this.senderName = message.getSenderName();
            this.receiverName = message.getReceiverName();
            this.content = message.getContent();
            this.createdAt = message.getCreatedAt();
            this.isRead = message.getIsRead();
        }
    }

    public static class ConversationResponse {
        public Long userId;
        public String userName;
        public String lastMessage;
        public LocalDateTime lastMessageAt;
        public long unreadCount;
    }

    public static class MarkAsReadRequest {
        public Long senderId;
        public Long receiverId;
    }
}
