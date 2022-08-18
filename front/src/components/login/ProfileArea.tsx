import React, { FC, useState, useEffect } from "react";
import {
  Box,
  Stack,
  Grid,
  Typography,
  Modal,
  TextField,
  Avatar,
} from "@mui/material";
import CheckIcon from "@mui/icons-material/Check";
import RefreshIcon from "@mui/icons-material/Refresh";
import { useSelector, useDispatch } from "react-redux";
import { ProfileRes } from "../../apis/response/memberRes";
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
import { useNavigate } from "react-router";
import { AvatimeApi } from "../../apis/avatimeApi";
import { AvatimeWs } from "../../apis/avatimeWs";
import { AlertSnackbar } from "../AlertSnackbar";
import { useFaceMask } from '../../hooks/useFaceMesh';
import { SoundIconButton } from "../SoundButton";

const style = {
  position: "absolute" as "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: 730,
  height: 320,
  bgcolor: "background.paper",
  border: "1px solid #000",
  boxShadow: 24,
  p: 4,
  borderRadius: "10px",
};

interface IProps {}

export const ProfileArea: FC<IProps> = (props) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const [open, setOpen] = React.useState(false);

  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);

  const socialId = useSelector((state: any) => state.user.socialId);
  const socialType = useSelector((state: any) => state.user.socialType);
  const userGender = useSelector((state: any) => state.user.userGender);
  const userId: number = useSelector((state: any) => state.user.userId);
  const userName = useSelector((state: any) => state.user.userName);
  const userDesc = useSelector((state: any) => state.user.userDesc);
  const profileImagePath = useSelector((state: any) => state.user.profileImagePath);
  const isLogin = useSelector((state: any) => state.user.isLogin);

  const [name, setName] = useState(userName);
  const [desc, setDesc] = useState(userDesc);
  const [image, setImage] = useState(profileImagePath);

  const [nameCheck, setNameCheck] = useState(true);
  const [nameText, setNameText] = useState("");
  const [overlap, setOverlap] = useState(true);
  const [nameSatis, setNameSatis] = useState(true);
  const [descText, setDescText] = useState("");
  const [overContents, setOverContents] = useState(true);
  const [descSatis, setDescSatis] = useState(true);
  const [profileImages, setProfileImages] = useState<ProfileRes[]>([]);

  const [showSnack, setShowSnack] = useState(false);
  const [showSnack2, setShowSnack2] = useState(false);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    AvatimeApi.getInstance().getProfileList({
      onSuccess(data) {
        setProfileImages(data);
      },
      navigate,
    });
  }, [navigate]);

  useEffect(() => {
    if (nameCheck) {
      // 생성가능
      setNameText(" ");
      setOverlap(false);
    } else if (name.trim() === userName) {
      setNameText("현재 이름과 같아요.");
      setOverlap(true);
    } else {
      // 생성불가능
      setNameText("중복된 이름이예요");
      setOverlap(true);
    }
  }, [nameCheck]);

  useEffect(() => {
    if (nameSatis) {
      setNameText(" ");
      setOverlap(false);
    } else {
      setNameText("이름을 2-10자 이내로 지어주세요.");
      setOverlap(true);
    }
  }, [nameSatis]);

  useEffect(() => {
    if (descSatis) {
      setDescText(desc.length + "/255");
      setOverContents(false);
    } else {
      setDescText("자기소개를 255자 이내로 작성해주세요.");
      setOverContents(true);
    }
  }, [descSatis, desc]);

  useEffect(() => {
    console.log(profileImages);
  }, [profileImages]);

  const handleNameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setName(event.target.value.trim());
    if (event.target.value.trim().length < 2 || event.target.value.trim().length > 10) {
      setNameSatis(false);
    } else {
      setNameSatis(true);

      AvatimeApi.getInstance().checkName(
        { name: event.target.value },
        {
          onSuccess(data) {
            setNameCheck(data);
          },
          navigate,
        }
      );
    }
  };

  const handleDescChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.value.length > 255) {
      setDescSatis(false);
    } else {
      setDescSatis(true);
    }
    setDesc(event.target.value);
  };

  const confirmInfo = () => {
    // 만족했는지 조건 추가
    if (!nameCheck || !nameSatis || !descSatis || !(!!name)) {
      setShowSnack(true);
    } else {
      console.log("user_id : " + userId);
      console.log("name : " + name);
      console.log("profile_image_path : " + image);
      console.log("description : " + desc);
      isLogin
        ? AvatimeApi.getInstance().modifyUser(
            {
              id: userId,
              name: name,
              profile_image_path: image,
              description: desc,
            },
            {
              onSuccess(data) {
                console.log(data.statusCode);
                console.log(data.message);
                setMsg("정보 수정 완료!");
                setShowSnack2(true);
                dispatch(setProfileImagePath(image));
                dispatch(setUserName(name));
                dispatch(setUserDesc(desc));
              },
              navigate,
            }
          )
        : AvatimeApi.getInstance().register(
            {
              social_id: socialId,
              social_type: socialType,
              gender: userGender,
              name: name,
              profile_image_path: image,
              description: desc,
            },
            {
              onSuccess(data) {
                console.log(data);
                setMsg("회원가입 완료!");
                setTimeout(() => setShowSnack2(true), 2000);
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
              },
              navigate,
            }
          );
    }
  };

  const snackAfter = () => {
    navigate("/main");
  }

  const refreshForm = () => {
    setName(userName);
    setDesc(userDesc);
    setNameCheck(true);
    setNameSatis(true);
    setDescSatis(true);
  };

  const getProfile = (path: string) => {
    setImage(path);
    handleClose();
  };

  return (
    <>
      <Stack>
        <Box display="flex" justifyContent="center" alignItems="start" marginLeft="10vw">
          <SoundIconButton onClick={handleOpen}>
            <Avatar
              src={image}
              sx={{ width: 80, height: 80 }}
              style={{
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
              }}
            />
          </SoundIconButton>
          <Modal
            open={open}
            onClose={handleClose}
            aria-labelledby="modal-modal-title"
            aria-describedby="modal-modal-description"
          >
            <Box sx={style}>
              <Typography id="modal-modal-title" variant="h6" component="h2" textAlign="center">
                프로필 사진 바꾸기
              </Typography>
              <Typography id="modal-modal-description" textAlign="center"></Typography>
              <Box>
                {profileImages?.map((ProfileRes, idx) => {
                  return (
                    <SoundIconButton
                      key={idx}
                      onClick={() => getProfile(ProfileRes.image_path)}
                      sx={{ margin: 3 }}
                    >
                      <Avatar
                        src={ProfileRes.image_path}
                        sx={{ width: 80, height: 80 }}
                        style={{
                          display: "flex",
                          justifyContent: "center",
                          alignItems: "center",
                        }}
                      />
                    </SoundIconButton>
                  );
                })}
              </Box>
            </Box>
          </Modal>
          <Box display="inline" marginLeft="5.5vw" sx={{ alignItems: "flex-start" }}>
            <SoundIconButton onClick={confirmInfo}>
              <CheckIcon />
            </SoundIconButton>
            <SoundIconButton onClick={refreshForm}>
              <RefreshIcon />
            </SoundIconButton>
          </Box>
        </Box>
      </Stack>
      <Grid
        display="flex"
        justifyContent="center"
        alignItems="center"
        marginTop="7vh"
        marginBottom="5vh"
      >
        <TextField
          id="inputName"
          label="닉네임"
          type="string"
          value={name}
          placeholder="닉네임을 입력해주세요."
          autoFocus
          onChange={handleNameChange}
          helperText={nameText}
          error={overlap}
          sx={{
            width: "18vw",
          }}
        />
      </Grid>
      <Grid display="flex" justifyContent="center" alignItems="center">
        <TextField
          id="inputDesc"
          label="자기소개"
          type="string"
          value={desc}
          placeholder="자기소개를 입력해주세요."
          rows={6}
          multiline
          onChange={handleDescChange}
          helperText={descText}
          error={overContents}
          sx={{
            height: "20vh",
            width: "25vw",
          }}
        />
      </Grid>
      <AlertSnackbar
        open={showSnack}
        onClose={() => setShowSnack(false)}
        message="잘못된 항목이 있어요."
        alertColor="warning"
        type="alert"
      />
      <AlertSnackbar
        open={showSnack2}
        onClose={() => setShowSnack2(false)}
        message={msg}
        alertColor="success"
        type="confirm"
        onSuccess={snackAfter}
      />
    </>
  );
};
