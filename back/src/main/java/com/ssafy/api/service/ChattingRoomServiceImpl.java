package com.ssafy.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.db.entity.ChattingRoom;
import com.ssafy.db.repository.ChattingRoomRepository;

@Service("chattingRoomService")
public class ChattingRoomServiceImpl implements ChattingRoomService{
	@Autowired
	ChattingRoomRepository chattingRoomRepository;
	
	@Override
	@Transactional
	public ChattingRoom saveByWaitingRoom(Long roomId) {
		ChattingRoom chattingRoom = ChattingRoom.ByWaitingRoomBuilder()
				.roomId(roomId).build();
		return chattingRoomRepository.save(chattingRoom);
	}
	
	// 전체 채팅, 성별 채팅 생성
    public ChattingRoom createRoomInMeetingRoom(Long roomId) {
        ChattingRoom chattingRoom1 = ChattingRoom.ByMeettingRoomBuilder().roomId(roomId).type(0).build();
        chattingRoomRepository.save(chattingRoom1);
        ChattingRoom womenChatting = ChattingRoom.ByMeettingRoomBuilder().roomId(roomId).type(1).build();
        chattingRoomRepository.save(womenChatting);
        ChattingRoom menChatting = ChattingRoom.ByMeettingRoomBuilder().roomId(roomId).type(1).build();
        chattingRoomRepository.save(menChatting);
    	return chattingRoom1;
    }
	@Override
	public ChattingRoom findByRoomIdAndType(Long roomId, int type) {
		return chattingRoomRepository.findByRoomIdAndType(roomId, type).get();
	}
	
	@Override
	public List<ChattingRoom> findAllByRoomId(Long roomId) {
		Sort sort = sortById();
		return chattingRoomRepository.findAllByRoomIdAndType(roomId, 1, sort).get();
	}
	
	private Sort sortById() {
		return Sort.by(Sort.Direction.DESC, "id");
	}
}
