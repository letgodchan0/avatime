package com.ssafy.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 유저 로그인 API ([POST] /api/v1/auth/login) 요청에 필요한 리퀘스트 바디 정의.
 */
@Getter
@Setter
@ApiModel("UserLoginPostRequest")
public class UserLoginPostReq {
	@ApiModelProperty(name="유저 Name", example="ssafy_web")
	@JsonProperty("social_id")
	String socialId;
	
	@JsonProperty("social_type")
	int socialType;
	
	String gender;
	
	String token;

}
