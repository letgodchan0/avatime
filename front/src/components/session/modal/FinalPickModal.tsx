import { Box, Modal, Typography, Grid } from "@mui/material";
import React, { FC, useState } from "react";
import { useEffect } from "react";
import { useQuery } from "react-query";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router";
import sessionApi from "../../../apis/sessionApi";
import { AvatarProfile } from "./AvatarProfile";

interface IProps {
  isOpened: boolean;
}

export const FinalPickModal: FC<IProps> = ({ isOpened }) => {
  const totalUserList = useSelector((state: any) => state.meeting.userList);

  const [selectedUserId, setSelectedUserId] = useState(
    totalUserList[totalUserList.length / 2].userId
  );

  const [timer, setTimer] = useState(-1);

  useEffect(() => {
    if (isOpened) {
      setTimer(2);
    }
  }, [isOpened]);

  useEffect(() => {
    if (0 < timer) {
      const id = setInterval(() => {
        setTimer((prev) => prev - 1);
      }, 1000);
      return () => clearInterval(id);
    }
  }, [timer]);

  const { isLoading } = useQuery(
    "meeting/pick",
    () =>
      sessionApi.patchMeetingPick({
        meetingRoomId: 0,
        userId: 0,
        pickUserId: selectedUserId,
      }),
    {
      enabled: timer === 0,
      keepPreviousData: false,
    }
  );

  const navigate = useNavigate();
  useEffect(() => {
    if (timer === 0 && !isLoading) {
      navigate("/finalPickResult");
    }
  }, [timer, isLoading, navigate]);

  return (
    <FinalPickModalPresenter
      isOpened={isOpened}
      timer={timer}
      userList={totalUserList.slice(totalUserList.length / 2)}
      selectedUserId={selectedUserId}
      onClickAvatar={(userId) => setSelectedUserId(userId)}
      isLoading={isLoading}
    />
  );
};

interface IPresenterProps {
  isOpened: boolean;
  timer: number;
  userList: any[];
  selectedUserId: number;
  onClickAvatar: (userId: number) => void;
  isLoading: boolean;
}

const FinalPickModalPresenter: FC<IPresenterProps> = ({
  isOpened,
  timer,
  userList,
  selectedUserId,
  onClickAvatar,
  isLoading,
}) => {
  return (
    <Modal open={isOpened}>
      <Box
        sx={{
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: "70%",
          height: "55%",
          bgcolor: "background.paper",
          border: "1px solid #000000",
          boxShadow: 24,
          p: 4,
          borderRadius: "10px",
          display: "flex",
          flexDirection: "column",
          justifyContent: "stretch",
          alignItems: "center",
        }}
      >
        <Typography variant="h4">마음에 드는 상대를 골라주세요! {timer}</Typography>
        <Box p={3} />
        <Grid container width="100%" height="100%" spacing={3}>
          {userList?.map((it, idx) => (
            <Grid item key={idx} xs={3}>
              <AvatarProfile
                selected={selectedUserId === it.userId}
                onClick={() => onClickAvatar(it.userId)}
                avatarName={it.avatarName}
                avatarImagePath={it.avatarImagePath}
              />
            </Grid>
          ))}
        </Grid>
      </Box>
    </Modal>
  );
};
