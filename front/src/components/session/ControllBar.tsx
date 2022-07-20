import React, { FC, useState } from "react";
import { Box } from "@mui/system";
import { Button, Card } from "@mui/material";
import MicIcon from "@mui/icons-material/Mic";
import MicOffIcon from "@mui/icons-material/MicOff";
import VideocamIcon from "@mui/icons-material/Videocam";
import VideocamOffIcon from "@mui/icons-material/VideocamOff";
import SettingsIcon from "@mui/icons-material/Settings";
import ExitToAppIcon from "@mui/icons-material/ExitToApp";
import PeopleIcon from "@mui/icons-material/People";

type Type = "master" | "normal";
interface IProps {
  type: Type;
}

export const ControllBar: FC<IProps> = ({ type }) => {
  const [micStatus, setMicStatus] = useState(true);
  const onChangeMicStatus = () => {
    setMicStatus((prev) => !prev);
  };

  const [cameraStatus, setCameraStatus] = useState(true);
  const onChangeCameraStatus = () => {
    setCameraStatus((prev) => !prev);
  };

  const onClickSettings = () => {};

  const onClickPick = () => {};

  const onClickExit = () => {};

  return (
    <ControllBarPresenter
      type={type}
      micStatus={micStatus}
      onChangeMicStatus={onChangeMicStatus}
      cameraStatus={cameraStatus}
      onChangeCameraStatus={onChangeCameraStatus}
      onClickSettings={onClickSettings}
      onClickPick={onClickPick}
      onClickExit={onClickExit}
    />
  );
};

interface IPresenterProps {
  type: Type;
  micStatus: boolean;
  onChangeMicStatus: () => void;
  cameraStatus: boolean;
  onChangeCameraStatus: () => void;
  onClickSettings: () => void;
  onClickPick: () => void;
  onClickExit: () => void;
}

export const ControllBarPresenter: FC<IPresenterProps> = ({
  type,
  micStatus,
  onChangeMicStatus,
  cameraStatus,
  onChangeCameraStatus,
  onClickSettings,
  onClickPick,
  onClickExit,
}) => {
  return (
    <Card>
      <Box display="flex" flexDirection="row" justifyContent="space-between" p={2}>
        <Button
          variant="contained"
          color={micStatus ? "primary" : "error"}
          startIcon={micStatus ? <MicIcon /> : <MicOffIcon />}
          onClick={onChangeMicStatus}
        >
          마이크
        </Button>
        <Button
          variant="contained"
          color={cameraStatus ? "primary" : "error"}
          startIcon={cameraStatus ? <VideocamIcon /> : <VideocamOffIcon />}
          onClick={onChangeCameraStatus}
        >
          카메라
        </Button>
        <Button variant="contained" startIcon={<SettingsIcon />} onClick={onClickSettings}>
          설정
        </Button>
        {type === "master" && (
          <Button variant="contained" startIcon={<PeopleIcon />} onClick={onClickPick}>
            최종 선택
          </Button>
        )}
        <Button variant="contained" startIcon={<ExitToAppIcon />} onClick={onClickExit}>
          나가기
        </Button>
      </Box>
    </Card>
  );
};
