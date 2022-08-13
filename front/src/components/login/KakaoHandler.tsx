import React, { FC, useState } from "react";
import { useDispatch } from "react-redux";
import { KAKAO_AGREE_URL } from "../../apis/Auth";
import {
  setUserId,
  setUserName,
  setUserGender,
  setUserDesc,
  setSocialId,
  setSocialType,
  setProfileImagePath,
  setIsLogin,
  setToken,
} from "../../stores/slices/userSlice";
import { Backdrop, CircularProgress } from "@mui/material";
import { useNavigate } from "react-router";
import { AvatimeApi } from "../../apis/avatimeApi";
import { AvatimeWs } from '../../apis/avatimeWs';
import { AlertSnackbar } from '../AlertSnackbar';

interface Iprops {}

export const KakaoHandler: FC<Iprops> = (props) => {
  const dispatch = useDispatch();
  let code = new URL(window.location.href).searchParams.get("code");
  const navigate = useNavigate();
  const [showLoginSnack, setShowLoginSnack] = useState(false);
  const [showRegisterSnack, setShowRegisterSnack] = useState(false);
  const [showReConfirmSnack, setShowReConfirmSnack] = useState(false);

  const login = () => {
    navigate("/main");
  }

  const register = () => {
    navigate("/mypage");
  }

  const reconfirm = () => {
    window.location.replace(KAKAO_AGREE_URL);
  }

  AvatimeApi.getInstance().kakaoLogin(code as string, {
    onSuccess(data) {
      if (data.statusCode === 201) {
        console.log(data);
        dispatch(setUserGender(data.gender));
        console.log(data.socialId);
        dispatch(setSocialId(data.social_id));
        console.log(data.socialType);
        dispatch(setSocialType(data.social_type));
        dispatch(setIsLogin(false));
        //navigate("/mypage");
        //alert("회원가입이 필요합니다.");
        setShowRegisterSnack(true);
      } else if (data.statusCode === 200) {
        console.log(data);
        dispatch(setUserId(data.user_id));
        dispatch(setUserName(data.name));
        dispatch(setUserGender(data.gender));
        dispatch(setUserDesc(data.description));
        dispatch(setProfileImagePath(data.profile_image_path));
        dispatch(setSocialId(data.social_id));
        dispatch(setSocialType(data.social_type));
        dispatch(setIsLogin(true));
        dispatch(setToken(data.accessToken));
        AvatimeApi.getInstance().login(data.accessToken);
        AvatimeWs.getInstance().login(data.accessToken);
        localStorage.setItem("token", data.accessToken);
        //navigate("/main");
        //alert("로그인 성공");
        setShowLoginSnack(true);
      } else if(data.statusCode === 205) {
        setShowReConfirmSnack(true);
      }
    },
    navigate,
  })
  // 인가코드

  return (
    <>
    <Backdrop open={true}>
      <CircularProgress />
    </Backdrop>
    <AlertSnackbar
        open={showLoginSnack}
        onClose={() => setShowLoginSnack(false)}
        message="로그인 성공"
        alertColor="success"
        type="confirm"
        onSuccess={login}
      />
     <AlertSnackbar
        open={showRegisterSnack}
        onClose={() => setShowRegisterSnack(false)}
        message="회원가입이 필요합니다."
        alertColor="warning"
        type="confirm"
        onSuccess={register}
      />
      <AlertSnackbar
        open={showReConfirmSnack}
        onClose={() => setShowReConfirmSnack(false)}
        message="로그인 실패! 정보 제공 동의 후 다시 시도해주세요."
        alertColor="warning"
        type="confirm"
        onSuccess={reconfirm}
      />
    </>
  );
};
