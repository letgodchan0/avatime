package com.ssafy.db.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.db.entity.MeetingRoom;


public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    // 아래와 같이, Query Method 인터페이스(반환값, 메소드명, 인자) 정의를 하면 자동으로 Query Method 구현됨.
    Optional<MeetingRoom> findBymainSessionId(Long main_session_id);
    Optional<List<MeetingRoom>> findAllBymainSessionIdAndType(Long main_session_id, int type);
    Optional<List<MeetingRoom>> findAllBymainSessionIdAndTypeAndStatus(Long main_session_id, int type, int status);
}