package com.ssafy.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Immutable
@Subselect("select w.waiting_room_id, " +"count(case when u.gender = 'F' then 1 end) as F, " +
			"count(case when u.gender = 'M' then 1 end) as M, " +
			"count(*) as total from waiting_room_user_relation w " +
			"left join user u on w.user_id = u.id " +
			"where w.type < 2 " +
			"group by w.waiting_room_id;")
@Synchronize({"waiting_room_user_relation w", "user"})
public class Gender extends BaseEntity{
	
	@Column(nullable = false)
	private Long waitingRoomId;
	
	@Column(nullable = false)
	private Long F;
	
	@Column(nullable = false)
	private Long M;
	
	@Column(nullable = false)
	private Long Total;
}
