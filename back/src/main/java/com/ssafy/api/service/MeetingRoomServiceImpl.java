package com.ssafy.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.ssafy.api.response.AvatarChoiceRes;
import com.ssafy.api.response.BalanceResultRes;
import com.ssafy.api.response.StuffChoiceRes;
import com.ssafy.api.response.entity.AvatarStatus;
import com.ssafy.api.response.entity.BalanceResult;
import com.ssafy.api.response.entity.StuffStatus;
import com.ssafy.db.entity.Avatar;
import com.ssafy.db.entity.Balance;
import com.ssafy.db.entity.BalanceRelation;
import com.ssafy.db.entity.MeetingRoom;
import com.ssafy.db.entity.MeetingRoomUserRelation;
import com.ssafy.db.entity.Stuff;
import com.ssafy.db.entity.WaitingRoomUserRelation;
import com.ssafy.db.repository.BalanceRelationRepository;
import com.ssafy.db.repository.ChattingRoomRepository;
import com.ssafy.db.repository.MeetingRoomRepository;
import com.ssafy.db.repository.MeetingRoomUserRelationRepository;
import com.ssafy.db.repository.UserRepository;
import com.ssafy.db.repository.WaitingRoomRepository;
import com.ssafy.db.repository.WaitingRoomUserRelationRepository;

import lombok.RequiredArgsConstructor;

/**
 *	미팅 관련 비즈니스 로직 처리를 위한 서비스 구현 정의.
 */
@RequiredArgsConstructor
@Service("meetingService")
public class MeetingRoomServiceImpl implements MeetingRoomService {
	private final String address = "https://avatimebucket2022.s3.ap-northeast-2.amazonaws.com/";
	
	@Autowired
	MeetingRoomRepository meetingRoomRepository;
	
	@Autowired
	MeetingRoomUserRelationRepository meetingRoomUserRelationRepository;
	
	@Autowired
	WaitingRoomRepository waitingRoomRepository;
	
	@Autowired
	WaitingRoomUserRelationRepository waitingRoomUserRelationRepository;
	
	@Autowired
	ChattingRoomRepository chattingRoomRepository;
	
	@Autowired
	ChattingRoomService chattingRoomService;
	
	@Autowired
	AvatarService avatarService;
	
	@Autowired
	StuffService stuffService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	BalanceRelationRepository balanceRelationRepository;
	
	private final SimpMessageSendingOperations sendingOperations;
	
	@Override
	public MeetingRoom createMeetingRoomSession(int type, Long mainSessionId) throws Exception {
		// TODO Auto-generated method stub
		// main session
		MeetingRoom meetingRoom = new MeetingRoom();
		try {
			meetingRoom.setType(type);
			if(type == 0) {
				mainSessionId = meetingRoomRepository.saveAndFlush(meetingRoom).getId();
				chattingRoomService.createRoomInMeetingRoom(mainSessionId);
			}
			else if(type == 1) {
				
			}
			meetingRoom.setMainSessionId(mainSessionId);
			meetingRoomRepository.save(meetingRoom);
		} catch (Exception e) {
		}
		
		return meetingRoom;
	}

	@Override
	public void choiceAvatar(Long meetingRoomId, Long userId, Long avatarId) throws Exception {
		// TODO Auto-generated method stub
		MeetingRoomUserRelation meetingRoomUserRelation = meetingRoomUserRelationRepository.findByMeetingRoomIdAndUserId(meetingRoomId, userId).orElse(null);
		if(meetingRoomUserRelation != null) {
			meetingRoomUserRelation.setAvatarId(avatarId);
			meetingRoomUserRelationRepository.saveAndFlush(meetingRoomUserRelation);
			sendAvatarInfo(meetingRoomId);
		}
	}

	@Override
	public void finalChoice(Long meetingRoomId, Long userId, Long pickUserId) throws Exception {
		// TODO Auto-generated method stub
		MeetingRoomUserRelation meetingRoomUser = meetingRoomUserRelationRepository.findByMeetingRoomIdAndUserId(meetingRoomId, userId).get();
		meetingRoomUser.setPickUserId(pickUserId);
		meetingRoomUserRelationRepository.saveAndFlush(meetingRoomUser);
		MeetingRoomUserRelation pickedUserInfo = meetingRoomUserRelationRepository.findByMeetingRoomIdAndUserId(meetingRoomId, pickUserId).get();
		if(pickedUserInfo.getPickUserId() != null && pickedUserInfo.getPickUserId().equals(userId)) {
			matched(meetingRoomUser, pickedUserInfo, meetingRoomId);
		} else {
			meetingRoomUser.setMatched(false);
		}
		meetingRoomUserRelationRepository.saveAndFlush(meetingRoomUser);
	}
	
	public void matched(MeetingRoomUserRelation meetingRoomUser, MeetingRoomUserRelation pickedUserInfo, Long meetingRoomId) throws Exception {
		meetingRoomUser.setMatched(true);
		pickedUserInfo.setMatched(true);
		meetingRoomUserRelationRepository.saveAndFlush(pickedUserInfo);
		MeetingRoom subMeetingRoom = createMeetingRoomSession(1, meetingRoomId);
		MeetingRoomUserRelation meetingRoomUserRelation1 = MeetingRoomUserRelation.builder()
				.meetingRoom(subMeetingRoom)
				.avatarId(meetingRoomUser.getAvatarId())
				.user(meetingRoomUser.getUser())
				.build();
		meetingRoomUserRelationRepository.saveAndFlush(meetingRoomUserRelation1);
		MeetingRoomUserRelation meetingRoomUserRelation2 = MeetingRoomUserRelation.builder()
				.meetingRoom(subMeetingRoom)
				.avatarId(pickedUserInfo.getAvatarId())
				.user(pickedUserInfo.getUser())
				.build();
		meetingRoomUserRelationRepository.saveAndFlush(meetingRoomUserRelation2);
	}

	@Override
	public List<MeetingRoomUserRelation> finalChoiceResult(Long meetingRoomId) throws Exception {
		// TODO Auto-generated method stub
		return meetingRoomUserRelationRepository.findAllByMeetingRoomIdAndLeftMeeting(meetingRoomId, false);
	}

	@Override
	public boolean isSelectedAvatar(Long meetingRoomId, Long avatarId) throws Exception {
		// TODO Auto-generated method stub
		return meetingRoomUserRelationRepository.existsByMeetingRoomIdAndAvatarId(meetingRoomId, avatarId);
	}
	
	public Long createMeetingRoom(Long waitingRoomId) throws Exception {
		// meeting room 생성
		MeetingRoom meetingRoom = createMeetingRoomSession(0, waitingRoomId);
		meetingRoom.setHeadCount(waitingRoomRepository.findById(waitingRoomId).get().getHeadCount());
		meetingRoomRepository.saveAndFlush(meetingRoom);
		Long meetingRoomId = meetingRoom.getId();
		// meetingroomuserrelation 삽입
		List<WaitingRoomUserRelation> list = waitingRoomUserRelationRepository.findByWaitingRoomId(waitingRoomId);
		for(WaitingRoomUserRelation user : list) {
			if(user.getType() == 1 || user.getType() == 0) {
				MeetingRoomUserRelation meetingRoomUserRelation = MeetingRoomUserRelation.builder()
						.meetingRoom(meetingRoom)
						.user(user.getUser())
						.build();
				meetingRoomUserRelationRepository.save(meetingRoomUserRelation);
			}
		}
		// meetingroomid 리턴
		return meetingRoomId;
	}

	@Override
	public void timer(Long meetingRoomId, int time, String type) throws Exception {
		// TODO Auto-generated method stub
		if(type.equals("pick")) {
			MeetingRoom meetingRoom = meetingRoomRepository.findById(meetingRoomId).get();
			meetingRoom.setStatus(1);
			meetingRoomRepository.save(meetingRoom);
		}
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			int count = time;
			
			@Override
			public void run() {
				if(count >= 0) {
			    	try { sendingOperations.convertAndSend("/topic/meeting/"+type+"/timer/"+meetingRoomId, count);}
			    	catch(Exception e) {}
					count--;
				} else {
					timer.cancel();
					if(type.equals("avatar"))
						try {
							pickAvatar(meetingRoomId);
						} catch (Exception e) {
							// TODO Auto-generated catch block	
						}
					else if (type.equals("stuffSubSession")) {
						List<MeetingRoomUserRelation> list = meetingRoomUserRelationRepository.findAllByMeetingRoomId(meetingRoomId);
						for(MeetingRoomUserRelation user : list) {
							user.setStuffId(0L);
							meetingRoomUserRelationRepository.saveAndFlush(user);
						}
					}
					else if(type.equals("pick")) {
						List<MeetingRoomUserRelation> userList = meetingRoomUserRelationRepository.findAllByMeetingRoomIdAndLeftMeeting(meetingRoomId, false);
						for(MeetingRoomUserRelation user : userList) {
							if(userRepository.findById(user.getId()).get().getGender().equals("M")) continue;
							MeetingRoomUserRelation pickedUserInfo = findUser(meetingRoomId, user.getPickUserId());
							if(user.getId().equals(pickedUserInfo.getPickUserId())) {
								try {
									matched(user, pickedUserInfo, meetingRoomId);
								} catch (Exception e) {
									// TODO Auto-generated catch block
								}
							}
						}
					}
				}
			}

		};
		
		timer.schedule(task, 1000, 1000);
	}
	
	@Override
	public int userNumber(Long meetingRoomId) throws Exception {
		// TODO Auto-generated method stub
		return meetingRoomUserRelationRepository.countByMeetingRoomIdAndLeftMeeting(meetingRoomId, false);
	}
	
	@Override
	public MeetingRoomUserRelation findUser(Long meetingRoomId, Long userId) {
		// TODO Auto-generated method stub
		return meetingRoomUserRelationRepository.findByMeetingRoomIdAndUserId(meetingRoomId, userId).get();
	}

	@Override
	public void save(MeetingRoomUserRelation meetingRoomUserRelation) {
		// TODO Auto-generated method stub
		meetingRoomUserRelationRepository.saveAndFlush(meetingRoomUserRelation);
	}
	
	@Override
	public void save(MeetingRoom meetingRoom) {
		meetingRoomRepository.save(meetingRoom);
	}
	
	@Override
	public void pickAvatar(Long meetingRoomId) throws Exception {
		// TODO Auto-generated method stub
		List<MeetingRoomUserRelation> userList = meetingRoomUserRelationRepository.findAllByMeetingRoomIdAndLeftMeeting(meetingRoomId, false);
		
		for(MeetingRoomUserRelation user : userList) {
			if(user.getAvatarId() == null) {
				for(Long avatarId = 1L; avatarId<= avatarService.findAll().size(); avatarId++) {
					if(!isSelectedAvatar(meetingRoomId, avatarId)) {
						user.setAvatarId(avatarId);
						meetingRoomUserRelationRepository.save(user);
						break;
					}
				}
			}
		}
		
		sendAvatarInfo(meetingRoomId);
	}
	
	@Override
	public int sendAvatarInfo(Long meetingRoomId) throws Exception {
		AvatarChoiceRes avatarChoiceRes = new AvatarChoiceRes();
		List<Avatar> avatarList = avatarService.findAllByUserId(0L);
		List<AvatarStatus> list = new ArrayList<>();
		for(Avatar ava : avatarList) {
			AvatarStatus avasta = new AvatarStatus(ava);
			if(isSelectedAvatar(meetingRoomId, ava.getId())) {
				avasta.setSelected(true);
			}
			else avasta.setSelected(false);
			list.add(avasta);
		}
		avatarChoiceRes.setStatus(meetingRoomUserRelationRepository.countByMeetingRoomIdAndAvatarIdNotNull(meetingRoomId) == userNumber(meetingRoomId) ? 1 : 0);
		avatarChoiceRes.setAvatar_list(list);
		
    	sendingOperations.convertAndSend("/topic/meeting/avatar/"+meetingRoomId, avatarChoiceRes);
    	return avatarChoiceRes.getStatus();
	}

	@Override
	public MeetingRoom findById(Long meetingRoomId) {
		// TODO Auto-generated method stub
		return meetingRoomRepository.findById(meetingRoomId).get();
	}

	@Override
	public List<MeetingRoomUserRelation> findAllByMeetingRoomId(Long meetingRoomId) throws Exception {
		// TODO Auto-generated method stub
		return meetingRoomUserRelationRepository.findAllByMeetingRoomId(meetingRoomId);
	}
	
	@Override
	public MeetingRoom findSubMeetingRoom(Long mainMeetingRoomId, Long userId) throws Exception {
		List<MeetingRoom> list = meetingRoomRepository.findAllBymainSessionIdAndTypeAndStatus(mainMeetingRoomId, 1, 0).get();
		for(MeetingRoom meetingRoom : list) {
			if(meetingRoomUserRelationRepository.existsByMeetingRoomIdAndUserId(meetingRoom.getId(), userId)) return meetingRoom;
		}
		
		return null;
	}

	/**
	 * true : gender에 해당하는 사람이 선택 불가능
	 * false : gender에 해당하는 사람이 선택 가능
	 */
	@Override
	public boolean isSelectedStuff(Long meetingRoomId, String gender, Long stuffId) throws Exception {
		// TODO Auto-generated method stub
		List<MeetingRoomUserRelation> list = meetingRoomUserRelationRepository.findByMeetingRoomIdAndStuffId(meetingRoomId, stuffId).orElse(null);
		if(list == null || list.size() == 0) return false;
		else if(list.size() == 2) return true;
		else return list.get(0).getUser().getGender().equals(gender);
	}

	@Override
	public void choiceStuff(Long meetingRoomId, Long userId, Long stuffId) throws Exception {
		// TODO Auto-generated method stub
		MeetingRoomUserRelation meetingRoomUserRelation = meetingRoomUserRelationRepository.findByMeetingRoomIdAndUserId(meetingRoomId, userId).orElse(null);
		if(meetingRoomUserRelation != null) {
			meetingRoomUserRelation.setStuffId(stuffId);
			meetingRoomUserRelationRepository.saveAndFlush(meetingRoomUserRelation);
			sendStuffInfo(meetingRoomId);
		}
	}
	
	@Override
	public int sendStuffInfo(Long meetingRoomId) throws Exception {
		int num = 0;
		MeetingRoom meetingRoom = meetingRoomRepository.findById(meetingRoomId).get();
		StuffChoiceRes stuffChoiceRes = new StuffChoiceRes();
		List<Stuff> stuffList = stuffService.findAll();
		List<StuffStatus> list = new ArrayList<>();
		for(int i = 0; i<meetingRoom.getHeadCount()/2; i++) {
			Stuff stuff = stuffList.get(i);
			StuffStatus stuffsta = new StuffStatus(stuff);
			if(isSelectedStuff(meetingRoomId, "M", stuff.getId())) {
				stuffsta.setMen_selected(true);
				num++;
			} else stuffsta.setMen_selected(false);
			if(isSelectedStuff(meetingRoomId, "F", stuff.getId())) {
				stuffsta.setWomen_selected(true);
				num++;
			} else stuffsta.setWomen_selected(false);
			
			list.add(stuffsta);
		}
		stuffChoiceRes.setStatus(num == userNumber(meetingRoomId) ? 1 : 0);
		stuffChoiceRes.setStuff_list(list);
		
    	sendingOperations.convertAndSend("/topic/meeting/stuff/"+meetingRoomId, stuffChoiceRes);
    	if(stuffChoiceRes.getStatus() == 1) timer(meetingRoomId, 120, "stuffSubSession");
    	return stuffChoiceRes.getStatus();
	}

	@Override
	public int selectStuffNum(Long meetingRoomId) throws Exception {
		// TODO Auto-generated method stub
		MeetingRoom meetingRoom = meetingRoomRepository.findById(meetingRoomId).get();
		int stuff = meetingRoom.getStuff();
		meetingRoom.setStuff(stuff+1);
		meetingRoomRepository.saveAndFlush(meetingRoom);
		return stuff;
	}

	@Override
	public void sendBalanceResult(Long meetingRoomId, Balance balance) throws Exception {
		// TODO Auto-generated method stub
		List<BalanceRelation> list = balanceRelationRepository.findAllByMeetingRoomIdAndBalance(meetingRoomId, balance);
		BalanceResultRes response = new BalanceResultRes();
		List<BalanceResult> resultList = new ArrayList<>();
		for(BalanceRelation b : list) {
			BalanceResult result = BalanceResult.builder()
					.user_id(b.getUserId())
					.result(b.isResult())
					.build();
			resultList.add(result);
		}
		response.setBalance_result(resultList);
		sendingOperations.convertAndSend("/topic/meeting/balance/result/"+meetingRoomId, response);
	}
}
