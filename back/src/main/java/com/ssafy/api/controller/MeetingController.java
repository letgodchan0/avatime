package com.ssafy.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.api.request.BalancePostReq;
import com.ssafy.api.request.BalanceStartReq;
import com.ssafy.api.request.FinalChoiceUserReq;
import com.ssafy.api.request.LeavingMeetingRoomReq;
import com.ssafy.api.request.MeetingRoomIdReq;
import com.ssafy.api.request.UserSelectAvatarReq;
import com.ssafy.api.request.UserSelectStuffReq;
import com.ssafy.api.response.FinalChoiceRes;
import com.ssafy.api.response.LastPickStatusRes;
import com.ssafy.api.response.MeetingRoomInfoRes;
import com.ssafy.api.response.MeetingRoomUserRes;
import com.ssafy.api.response.entity.Result;
import com.ssafy.api.service.AvatarService;
import com.ssafy.api.service.ChattingRoomService;
import com.ssafy.api.service.MeetingRoomService;
import com.ssafy.common.model.response.BaseResponseBody;
import com.ssafy.db.entity.Avatar;
import com.ssafy.db.entity.Balance;
import com.ssafy.db.entity.BalanceRelation;
import com.ssafy.db.entity.ChattingRoom;
import com.ssafy.db.entity.MeetingRoom;
import com.ssafy.db.entity.MeetingRoomUserRelation;
import com.ssafy.db.repository.BalanceRelationRepository;
import com.ssafy.db.repository.BalanceRepository;
import com.ssafy.db.repository.UserRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * ?????? ?????? API ?????? ????????? ?????? ???????????? ??????.
 */
@Api(value = "????????? ?????? API", tags = {"Meeting."})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/meeting")
public class MeetingController {
	private final SimpMessageSendingOperations sendingOperations;	
	
	@Autowired
	MeetingRoomService meetingRoomService;
	
	@Autowired
	ChattingRoomService chattingRoomService;
	
	@Autowired
//	UserService userService;
	UserRepository userRepository;
	
	@Autowired
	AvatarService avatarService;
	
	@Autowired
	BalanceRepository balanceRepository;
	
	@Autowired
	BalanceRelationRepository balanceRelationRepository;
	
	@PatchMapping("/selectAvatar")
	@ApiOperation(value = "????????? ??????", notes = "<strong>????????? ????????? ??????</strong>") 
    @ApiResponses({
        @ApiResponse(code = 201, message = "??????", response = BaseResponseBody.class),
        @ApiResponse(code = 409, message = "?????? ????????? ?????? ????????? ??????????????????", response = BaseResponseBody.class),
        @ApiResponse(code = 500, message = "?????? ??????", response = BaseResponseBody.class)
    })
	public ResponseEntity<?> choiceAvatar(@RequestBody @ApiParam(value="?????? ???????????? ????????? ?????????", required = true) UserSelectAvatarReq userSelectAvatar) {
		Long meetingRoomId = userSelectAvatar.getMeetingRoomId();
		Long userId = userSelectAvatar.getUserId();
		Long avatarId = userSelectAvatar.getAvatarId();
		
		try {
			if(meetingRoomService.isSelectedAvatar(meetingRoomId, avatarId)) return ResponseEntity.status(409).body("");
			else meetingRoomService.choiceAvatar(meetingRoomId, userId, avatarId);
		} catch(Exception e) {
			return ResponseEntity.status(500).body(e);
		}
		
		return ResponseEntity.status(201).body("");
	}
	
	@MessageExceptionHandler
	@MessageMapping("/meeting/avatar/{meetingRoomId}")
	public void startAvatarChoice(@DestinationVariable Long meetingRoomId) throws Exception {
		meetingRoomService.sendAvatarInfo(meetingRoomId);
	}
	
	@GetMapping("/{meetingroom_id}")
	@ApiOperation(value = "????????? ?????? ??????", notes = "<strong>meeting room id</strong>??? ?????? ????????? ?????? ??????") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "??????", response = BaseResponseBody.class),
        @ApiResponse(code = 500, message = "?????? ??????", response = BaseResponseBody.class)
    })
	public ResponseEntity<?> getMeetingRoomInfo(@PathVariable Long meetingroom_id) {
		try {
			MeetingRoom meetingRoom = meetingRoomService.findById(meetingroom_id);
			List<ChattingRoom> chatList = chattingRoomService.findAllByRoomId(meetingroom_id);
			MeetingRoomInfoRes meetingRoomInfoRes = MeetingRoomInfoRes.builder()
					.created_time(meetingRoom.getCreatedTime().toString())
					.chattingroom_id(chattingRoomService.findByRoomIdAndType(meetingroom_id, 0).getId())
					.men_chattingroom_id(chatList.get(0).getId())
					.women_chattingroom_id(chatList.get(1).getId())
					.meeting_user_info_list(new ArrayList<>())
					.build();
			
			List<MeetingRoomUserRelation> meetingRoomUserRelationList = meetingRoomService.findAllByMeetingRoomId(meetingroom_id);
			for (MeetingRoomUserRelation meetingRoomUserRelation : meetingRoomUserRelationList) {
				Avatar avatar = avatarService.findById(meetingRoomUserRelation.getAvatarId());
				MeetingRoomUserRes meetingRoomUserRes = MeetingRoomUserRes.builder()
						.user_id(meetingRoomUserRelation.getUser().getId())
						.user_name(meetingRoomUserRelation.getUser().getName())
						.avatar_id(avatar.getId())
						.avatar_name(avatar.getName())
						.avatar_image_path(avatar.getImagePath())
						.gender(meetingRoomUserRelation.getUser().getGender())
						.build();
				meetingRoomInfoRes.getMeeting_user_info_list().add(meetingRoomUserRes);
			}
				
			return ResponseEntity.status(200).body(meetingRoomInfoRes);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(e);
		}
	}

	@PatchMapping("/pick")
	@ApiOperation(value = "?????? ??????", notes = "<strong>meeting room id</strong>??? ?????? ?????? ?????? ??????") 
    @ApiResponses({
        @ApiResponse(code = 201, message = "??????", response = BaseResponseBody.class),
        @ApiResponse(code = 500, message = "?????? ??????", response = BaseResponseBody.class)
    })
	public ResponseEntity<?> finalChoice(@RequestBody @ApiParam(value="?????? ????????? ????????? ?????? ??????", required = true) FinalChoiceUserReq finalChoiceUserReq) {
		Long meetingRoomId = finalChoiceUserReq.getMeetingRoomId();
		Long userId = finalChoiceUserReq.getUserId();
		Long pickedUserId = finalChoiceUserReq.getPickUserId();
		try {
			meetingRoomService.finalChoice(meetingRoomId, userId, pickedUserId);
		} catch(Exception e) {
			return ResponseEntity.status(500).body(e);
		}
		
		return ResponseEntity.status(200).body("");
	}
	 
	@GetMapping("/pick/result/{meetingRoomId}/{userId}")
	@ApiOperation(value = "?????? ??????", notes = "<strong>meeting room id</strong>??? ?????? ?????? ?????? ??????") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "??????", response = BaseResponseBody.class),
        @ApiResponse(code = 500, message = "?????? ??????", response = BaseResponseBody.class)
    })
	public ResponseEntity<?> finalMeetingResult(@PathVariable Long meetingRoomId, @PathVariable Long userId) {
		FinalChoiceRes finalChoiceRes = new FinalChoiceRes();
		List<Result> list = new ArrayList<>();
		try {
			MeetingRoomUserRelation meetingRoomUserRelation = meetingRoomService.findUser(meetingRoomId, userId);
			finalChoiceRes.setMatched(meetingRoomUserRelation.isMatched());
			if(meetingRoomUserRelation.isMatched()) finalChoiceRes.setMeetingroom_id(meetingRoomService.findSubMeetingRoom(meetingRoomId, userId).getId());
			List<MeetingRoomUserRelation> userList = meetingRoomService.findAllByMeetingRoomId(meetingRoomId);
			String gender = userRepository.findById(userId).get().getGender();
			for(MeetingRoomUserRelation user : userList) {
				Avatar avatar = avatarService.findById(user.getAvatarId());
				Result result = Result.builder()
						.id(user.getUser().getId())
						.name(user.getUser().getName())
						.gender(user.getUser().getGender())
						.avatar_id(user.getAvatarId())
						.avatar_name(avatar.getName())
						.avatar_image_path(avatar.getImagePath())
						.pick_user_id(user.getPickUserId())
						.build();
				if(user.getUser().getGender().equals(gender)) list.add(0, result);
				else list.add(result);
			}
			finalChoiceRes.setResult_list(list);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(e);
		}
		return ResponseEntity.status(200).body(FinalChoiceRes.of(200, "?????? ?????? ???????????? ??????", finalChoiceRes));
	}
	
	@PostMapping("/pick/start")
	@ApiOperation(value = "?????? ?????? ??????", notes = "<strong>meeting room id</strong>????????? ?????? ?????? ??????") 
    @ApiResponses({
        @ApiResponse(code = 201, message = "??????", response = BaseResponseBody.class),
        @ApiResponse(code = 500, message = "?????? ??????", response = BaseResponseBody.class)
    })
	public ResponseEntity<?> finalPickStart(@RequestBody @ApiParam(value="????????? ??????", required = true) MeetingRoomIdReq meetingRoomIdReq) throws Exception {
		try {
			Long meetingRoomId = meetingRoomIdReq.getMeetingroom_id();
			MeetingRoom meetingRoom = meetingRoomService.findById(meetingRoomId);
			meetingRoom.setStatus(1);
			meetingRoomService.save(meetingRoom);
			sendLastPickStatus(meetingRoomId);
			meetingRoomService.timer(meetingRoomId, 15, "pick");
			return ResponseEntity.status(201).body("??????");
		}
		catch(Exception e) {
			return ResponseEntity.status(500).body("??????????????? ???????????????");
		}
	}
	
	@PostMapping("/leave")
	@ApiOperation(value = "????????? ?????????", notes = "<strong>meeting room id</strong>????????? ?????????") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "??????", response = BaseResponseBody.class),
        @ApiResponse(code = 500, message = "?????? ??????", response = BaseResponseBody.class)
    })
	public ResponseEntity<?> leavingMeeting(@RequestBody LeavingMeetingRoomReq leavingMeetingRoomReq) throws Exception {
		try {
			MeetingRoomUserRelation meetingRoomUser = meetingRoomService.findUser(leavingMeetingRoomReq.getMeetingRoomId(), leavingMeetingRoomReq.getUserId());
			meetingRoomUser.setLeftMeeting(true);
			meetingRoomService.save(meetingRoomUser);
			return ResponseEntity.status(201).body("??????");
		}
		catch (Exception e) {
			return ResponseEntity.status(500).body("??????????????? ???????????????");
		}
	}
	
	@MessageExceptionHandler
	@MessageMapping("/meeting/status")
	public void LastPickStatus(@DestinationVariable Long meetingRoomId) {
		sendLastPickStatus(meetingRoomId);
	}
	
	@MessageExceptionHandler
	public void sendLastPickStatus(Long meetingRoomId) {
		MeetingRoom meetingRoom = meetingRoomService.findById(meetingRoomId);
		LastPickStatusRes lastPickStatusRes = new LastPickStatusRes();
		lastPickStatusRes.setLast_pick_status(meetingRoom.getStatus() == 1);
		sendingOperations.convertAndSend("/topic/meeting/status/"+meetingRoomId, lastPickStatusRes);
	}
	
	@PatchMapping("/selectStuff")
	@ApiOperation(value = "?????? ??????", notes = "<strong>????????? ?????? ??????</strong>") 
    @ApiResponses({
        @ApiResponse(code = 201, message = "??????", response = BaseResponseBody.class),
        @ApiResponse(code = 409, message = "?????? ????????? ?????? ????????? ???????????????", response = BaseResponseBody.class),
        @ApiResponse(code = 500, message = "?????? ??????", response = BaseResponseBody.class)
    })
	public ResponseEntity<?> choiceStuff(@RequestBody @ApiParam(value="?????? ???????????? ?????? ?????????", required = true) UserSelectStuffReq userSelectStuff) {
		Long meetingRoomId = userSelectStuff.getMeetingRoomId();
		Long userId = userSelectStuff.getUserId();
		Long stuffId = userSelectStuff.getStuffId();
		String gender = userRepository.findById(userId).get().getGender();
		
		try {
			if(meetingRoomService.isSelectedStuff(meetingRoomId, gender, stuffId)) return ResponseEntity.status(409).body("");
			else meetingRoomService.choiceStuff(meetingRoomId, userId, stuffId);
		} catch(Exception e) {
			return ResponseEntity.status(500).body(e);
		}
		
		return ResponseEntity.status(201).body("");
	}
	
	@GetMapping("/stuff/start/{meetingroomId}")
	@ApiOperation(value = "?????? ????????? ??????", notes = "<strong>meeting room id</strong>??? ?????? ????????? ?????? ????????? ??????") 
    @ApiResponses({
        @ApiResponse(code = 201, message = "??????", response = BaseResponseBody.class),
        @ApiResponse(code = 409, message = "?????? ??????", response = BaseResponseBody.class),
        @ApiResponse(code = 500, message = "?????? ??????", response = BaseResponseBody.class)
    })
	public ResponseEntity<?> startSelectStuff(@PathVariable Long meetingroomId) {
		try {
			// ?????? ?????? 
			if(meetingRoomService.selectStuffNum(meetingroomId) > 3) return ResponseEntity.status(409).body("");
			meetingRoomService.sendStuffInfo(meetingroomId);
			meetingRoomService.timer(meetingroomId, 15, "stuff");
			return ResponseEntity.status(201).body("");
		} catch(Exception e) {
			return ResponseEntity.status(500).body(e);
		}
	}
	
	@MessageExceptionHandler
	@MessageMapping("/meeting/stuff/{meetingRoomId}")
	public void startStuffChoice(@DestinationVariable Long meetingRoomId) throws Exception {
		meetingRoomService.sendStuffInfo(meetingRoomId);
	}
	
	@PostMapping("/balance/start")
	@ApiOperation(value = "????????? ??????", notes = "????????? ????????? ???????????? ?????? ???????????????")
	public ResponseEntity<?> getBalance(@RequestBody BalanceStartReq balanceStart) {
		try {
			MeetingRoom meetingRoom = meetingRoomService.findById(balanceStart.getMeetingroomId());

			if (meetingRoom.getBalance() > 2) {
				return ResponseEntity.status(409).body("?????? ??????");
			}
			
			int range = 0;
			Long balanceId = 0L;
			boolean check = true;
			while (check) {
				Random r = new Random();
				range = (int) balanceRepository.count();
				balanceId = (Long) ((long) r.nextInt(range) + 1);
				check = balanceRelationRepository.existsByMeetingRoomIdAndBalanceId(balanceStart.getMeetingroomId(), balanceId);
			}
			meetingRoom.setBalance(meetingRoom.getBalance()+1);
			meetingRoomService.save(meetingRoom);
			Balance balance = balanceRepository.findById(balanceId).get();

			meetingRoomService.timer(balanceStart.getMeetingroomId(), 15, "balance");
			sendingOperations.convertAndSend("/topic/meeting/balance/"+balanceStart.getMeetingroomId(), balance);
			return ResponseEntity.status(200).body("??????");
		} catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("??????????????? ???????????????" + e);
		}
	}
	
	// ????????? ?????? ?????? ??????
	@PostMapping("/balance/result")
	public ResponseEntity<?> balance(@RequestBody BalancePostReq balance) throws Exception {
		try {
			// ?????? ???????????? balance ?????? ?????? 1 ??????
			Balance newBalance = balanceRepository.findById(balance.getBalanceId()).get();
			BalanceRelation balanceRelation = new BalanceRelation();
	
			balanceRelation.setMeetingRoomId(balance.getMeetingroomId());
			balanceRelation.setBalance(newBalance);
			balanceRelation.setUserId(balance.getUserId());
			balanceRelation.setResult(balance.isResult());
			
			balanceRelationRepository.save(balanceRelation);
			meetingRoomService.sendBalanceResult(balance.getMeetingroomId(), newBalance);
			return ResponseEntity.status(201).body("??????");
		} catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("??????????????? ???????????????" + e);
		}
	}

}
