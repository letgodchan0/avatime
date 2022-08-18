import { Box, Grid, Button, Typography } from '@mui/material';
import { grey } from "@mui/material/colors";
import React, { FC, useState, useEffect } from "react";
import CanvasDraw from "react-canvas-draw";
import { CanvasTools } from "../components/canvas/CanvasTools";
import { MainHeader } from "../components/main/MainHeader";
import { useRef } from "react";
import { AvatarProfile } from "../components/session/modal/AvatarProfile";
import { GetAvatarRes } from "../apis/response/avatarRes";
import { useSelector } from "react-redux";
import { AvatimeApi } from "../apis/avatimeApi";
import { useNavigate } from "react-router";
import { AlertSnackbar } from "../components/AlertSnackbar";

// 아바타의 임시 타입
// type TempAvatarRes = {
//   name: string; // 사용자가 정한 이름
//   path: string; // 지나님이 변환해준 이미지 경로
//   base64: string; // 여기 라이브러리에서 쓰는 데이터, 이걸 서버에서 저장 못한다고 하면 이미지 파일을 요 형식으로 변환하는 방법을 찾아봐야할 듯?
// };

interface IProps {}

export const CanvasPage: FC<IProps> = (props) => {
  const navigate = useNavigate();
  const userId: number = useSelector((state: any) => state.user.userId);

  const [brushColor, setBrushColor] = useState<string>("#000000");
  const [brushRadius, setBrushRadius] = useState<number>(5);
  const canvasRef = useRef<any>();

  const [showSuccessSnack, setShowSuccessSnack] = useState(false);
  const [showSnack, setShowSnack] = useState(false);
  const [msg, setMsg] = useState("");
  const [showConfirmSnack, setShowConfirmSnack] = useState(false);
  const [showPromptSnack, setShowPromptSnack] = useState(false);

  const [data, setData] = useState<string>();
  const [num, setNum] = useState(0);

  // 저장할 수 있는 아바타 칸이 4개라서 num이 1 ~ 4로 들어와요.
  // 서버 api도 num 번호에 따라 저장하도록 만들어 달라고 하시면 될 듯?
  const onSave = async (num: number) => {
    setNum(num);
    setShowPromptSnack(true);
  };

  const loadSavedAvatar = (data: string | undefined) => {
    if (!data) {
      return;
    }
    setData(data);
    setShowConfirmSnack(true);
  };

  // 여기에 서버에서 준 아바타 리스트 넣어주세요.
  // 대충 name, image path, 이미지로 변환하기 전의 base64 data가 있다고 가정하고 코드를 짰어요.
  const [avatarList, setAvatarList] = useState<GetAvatarRes[]>([]);

  const afterConfirm = () => {
    canvasRef.current.loadSaveData(data, true);
    setShowConfirmSnack(false);
  };

  const afterPrompt = async (avaname?: string) => {
    setShowPromptSnack(false);
    let flag = false;
    if (!avaname) {
      setMsg("아바타 이름을 입력해주세요.");
      setShowSnack(true);
      return;
    } else if (avaname.length > 4) {
      setMsg("4글자 이하로 이름을 지어주세요.");
      setShowSnack(true);
      return;
    } else {
      await AvatimeApi.getInstance().checkAvatarName(
        {
          name: avaname,
        },
        {
          onSuccess(data) {
            if (!data) {
              setMsg("중복된 아바타 이름이예요");
              setShowSnack(true);
              flag = true;
            }
          },
          navigate,
        }
      );
    }

    if (flag) return;

    const dataURL = canvasRef.current.getDataURL();
    const saveData = canvasRef.current.getSaveData();

    await AvatimeApi.getInstance().saveAvatar(
      {
        user_id: userId,
        name: avaname,
        slot: num,
        base64: dataURL,
        pic_info: saveData,
      },
      {
        onSuccess(data) {
          setShowSuccessSnack(true);

          // const newAvatar: GetAvatarRes = {
          //   id: data.id,
          //   name: avaname,
          //   path: data.path,
          //   base64: dataURL,
          //   slot: num,
          //   pic_info: saveData,
          // };
          // // 원래 슬롯에 있던 그림은 지워야함.
          // setAvatarList((prev) => [...prev.slice(0, num - 1), newAvatar, ...prev.slice(num - 1)]);
          AvatimeApi.getInstance().getAvatarList(
            { user_id: userId },
            {
              onSuccess(data) {
                setAvatarList(data);
              },
              navigate,
            }
          );
        },
        navigate,
      }
    );
  };

  useEffect(() => {
    if (!userId) {
      return;
    }
    AvatimeApi.getInstance().getAvatarList(
      { user_id: userId },
      {
        onSuccess(data) {
          setAvatarList(data);
        },
        navigate,
      }
    );
  }, [navigate, userId]);

  return (
    <Box className="mainback" display="flex" flexDirection="column">
      <MainHeader />
      <Box flex={1} p={5} display="flex" alignItems="stretch">
        <CanvasTools
          onChangeColor={setBrushColor}
          brushRadius={brushRadius}
          onChangeRadius={setBrushRadius}
          onUndo={() => canvasRef.current.undo()}
          onEraseAll={() => canvasRef.current.eraseAll()}
          onSave={onSave}
        />
        <Box p={1} />
        <Box flex={1} display="flex" flexDirection="column" alignItems="stretch" >
          <Typography color="error">
            처음 배경색은 투명이예요!!
          </Typography>
          <CanvasDraw
            ref={canvasRef}
            style={{
              flex: 1,
              width: "100%",
              height: "100%",
              borderRadius: "20px",
            }}
            imgSrc={process.env.PUBLIC_URL + "/canvasBg.png"}
            brushColor={brushColor}
            brushRadius={brushRadius}
            lazyRadius={1}
          />
        </Box>

        <Box p={1} />
        <Box
          flex={1}
          bgcolor={grey[200]}
          borderRadius="10px"
          p={2}
          display="flex"
          flexDirection="column"
        >
          {[0, 1].map((outerIdx) => (
            <Grid container spacing={2} flex="1" key={outerIdx}>
              {[0, 1].map((innerIdx) => {
                const idx = outerIdx * 2 + innerIdx;
                const avatar = idx < avatarList.length ? avatarList[idx] : null;
                return (
                  <Grid item xs={6} key={innerIdx}>
                    <AvatarProfile
                      selected={false}
                      onClick={() => loadSavedAvatar(avatar?.pic_info)}
                      avatarName={avatar ? avatar.name : "비어 있음"}
                      avatarImagePath={avatar ? avatar.path : ""}
                      opacity={0.1}
                    />
                  </Grid>
                );
              })}
            </Grid>
          ))}
        </Box>
      </Box>
      <AlertSnackbar
        open={showSuccessSnack}
        onClose={() => setShowSuccessSnack(false)}
        message="저장 성공!"
        alertColor="success"
        type="alert"
      />
      <AlertSnackbar
        open={showSnack}
        onClose={() => setShowSnack(false)}
        message={msg}
        alertColor="warning"
        type="alert"
      />
      <AlertSnackbar
        open={showConfirmSnack}
        onClose={() => setShowConfirmSnack(false)}
        message="아바타를 불러오시겠어요??"
        alertColor="info"
        type="confirm"
        onSuccess={afterConfirm}
      />
      <AlertSnackbar
        open={showPromptSnack}
        onClose={() => setShowPromptSnack(false)}
        message="아바타 이름을 알려주세요."
        type="prompt"
        onSuccess={afterPrompt}
        alertColor="info"
      />
    </Box>
  );
};
