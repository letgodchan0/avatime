package com.ssafy.db.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * 유저 모델 정의.
 */
@DynamicInsert @DynamicUpdate
@Entity
@Getter
@Setter
public class User extends BaseEntity{
	@Column(updatable = false, nullable = false)
    private boolean gender;
	
    @Column(nullable = false, unique = true, length = 20)
    private String name;
    
    private String desc;
	
    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Long profile_id;
	
    @Column(updatable = false, nullable = false)
    private String social_type;
	
    @Column(updatable = false, nullable = false)
    private String social_id;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp created_time;
    
    @PrePersist
    protected void onCreate() {
    	created_time = Timestamp.valueOf(LocalDateTime.now());
    }
}
