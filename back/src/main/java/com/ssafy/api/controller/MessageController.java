package com.ssafy.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.api.dto.ChatMessage;
import com.ssafy.api.request.ChattingMessagePostReq;
import com.ssafy.api.response.ChattingMessageRes;
import com.ssafy.api.service.ChatService;
import com.ssafy.api.service.ChattingMessageService;
import com.ssafy.api.service.UserService;
import com.ssafy.db.entity.ChattingMessage;
import com.ssafy.db.repository.ChattingMessageRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatting")
public class MessageController {

    private final SimpMessageSendingOperations sendingOperations;

    @Autowired
    private final ChatService chattingRoomService;
    @Autowired
    private final ChattingMessageService chattingMessageService;
    @Autowired
    private final ChattingMessageRepository chattingMessageRepository;
    @Autowired
    private final UserService userService;
    
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChattingMessagePostReq messageReq) throws Exception {
    	try {
    		if (ChattingMessagePostReq.MessageType.ENTER.equals(messageReq.getChat_type())) {
        		messageReq.setMessage(chattingMessageService.findUserName(chattingRoomService.findByChatId(messageReq.getChattingroom_id()), messageReq.getUser_id())+"님이 입장하였습니다.");
            } 
        	else if (ChattingMessagePostReq.MessageType.LEAVE.equals(messageReq.getChat_type())) {
        		messageReq.setMessage(chattingMessageService.findUserName(chattingRoomService.findByChatId(messageReq.getChattingroom_id()), messageReq.getUser_id())+"님이 퇴장하였습니다.");
            }
        	ChattingMessage message = ChattingMessage.builder()
        			.user(userService.getUserByUserId(messageReq.getUser_id()))
                    .type(messageReq.getChat_type().toString())
        			.content(messageReq.getMessage())
        			.chattingRoom(chattingRoomService.findByChatId(messageReq.getChattingroom_id()))
        			.build();
        	message = chattingMessageRepository.save(message);
        	
        	List<ChattingMessage> messages = chattingMessageService.findAllByChattingRoom(message.getChattingRoom());
        	List<ChattingMessageRes> list = new ArrayList<>();
        	for(ChattingMessage m : messages) {
        		ChattingMessageRes chat = ChattingMessageRes.builder()
        				.chat_type(m.getType())
            			.user_id(m.getUser().getId())
            			.name(chattingMessageService.findUserName(message.getChattingRoom(), m.getUser().getId()))
            			.message(m.getContent())
            			.created_time(m.getCreatedTime().toString())
            			.build();
        		list.add(chat);
        	}
        	sendingOperations.convertAndSend("/topic/chatting/receive/"+message.getChattingRoom().getId(), list);
        	return ResponseEntity.status(200).body("");
    	}
    	catch(Exception e) {
    		return ResponseEntity.status(500).body(e);
    	}
    }
    
    // 테스트용 코드
    @MessageExceptionHandler
    @MessageMapping("/chat/message")
    public void enter(ChatMessage message) {
		if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            message.setMessage(message.getUserName()+"님이 입장하였습니다.");
        }
        if (ChatMessage.MessageType.LEAVE.equals(message.getType())) {
            message.setMessage(message.getUserName()+"님이 퇴장하였습니다.");
        }
        ChattingMessage chat = ChattingMessage.builder()
        		.type(message.getType().toString())
        		.user(userService.getUserByUserId(1L))
        		.chattingRoom(chattingRoomService.findByChatId(message.getChatRoomId()))
        		.content(message.getMessage())
        		.build();
        chattingMessageRepository.save(chat);
        
        sendingOperations.convertAndSend("/topic/chatting/receive/"+message.getChatRoomId(),message);

    }
}
