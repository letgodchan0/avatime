import { Box, Typography, Grid, Stack, useTheme } from "@mui/material";
import React, { FC, useState } from "react";
import { useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useNavigate } from "react-router";
import { AvatarProfile } from "./AvatarProfile";
import { SessionModal } from "./SessionModal";
import { setPickUserName } from "../../../stores/slices/meetingSlice";
import { MeetingUserInfoRes } from "../../../apis/response/sessionRes";
import { useWebSocket } from "../../../hooks/useWebSocket";
import { AvatimeApi } from "../../../apis/avatimeApi";
import { Timer } from "../../timer/Timer";

interface IProps {
  isOpened: boolean;
}

export const FinalPickModal: FC<IProps> = ({ isOpened }) => {
  const totalUserList = useSelector((state: any) => state.meeting.userInfoList);
  const meetingRoomId = useSelector((state: any) => state.meeting.roomId);
  const userId = useSelector((state: any) => state.user.userId);
  const gender = useSelector((state: any) => state.user.userGender);

  const [targetUserList, setTargetUserList] = useState<MeetingUserInfoRes[]>();
  const [selectedUserId, setSelectedUserId] = useState(0);
  useEffect(() => {
    if (!totalUserList || !gender) {
      return;
    }

    const targetUserList = totalUserList.filter((it: any) => it.gender !== gender);
    setTargetUserList(targetUserList);
    setSelectedUserId(targetUserList[0].user_id);
  }, [totalUserList, gender, setSelectedUserId]);

  const [timer, setTimer] = useState(15);

  useWebSocket({
    onConnect: (frame, client) => {
      client.subscribe(`/topic/meeting/pick/timer/${meetingRoomId}`, function (response) {
        setTimer(JSON.parse(response.body));
      });
      client.publish({ destination: `/app/meeting/pick/timer/${meetingRoomId}` });
    },
    beforeDisconnected: (frame, client) => {},
  });

  const dispatch = useDispatch();
  const navigate = useNavigate();
  useEffect(() => {
    if (timer !== 0 || !targetUserList) {
      return;
    }

    dispatch(
      setPickUserName(targetUserList.find((it) => it.user_id === selectedUserId)!.user_name)
    );
    AvatimeApi.getInstance().patchFinalPick(
      {
        meetingroom_id: meetingRoomId,
        user_id: userId,
        pick_user_id: selectedUserId,
      },
      {
        onSuccess() {
          navigate("/finalPickResult", { replace: true });
        },
        navigate,
      }
    );
  }, [timer, navigate, meetingRoomId, userId, selectedUserId, dispatch, targetUserList]);

  return (
    <FinalPickModalPresenter
      isOpened={isOpened}
      timer={timer}
      targetUserList={targetUserList!}
      selectedUserId={selectedUserId}
      onClickAvatar={(userId) => setSelectedUserId(userId)}
    />
  );
};

interface IPresenterProps {
  isOpened: boolean;
  timer: number;
  targetUserList: any[];
  selectedUserId: number;
  onClickAvatar: (userId: number) => void;
}

const FinalPickModalPresenter: FC<IPresenterProps> = ({
  isOpened,
  timer,
  targetUserList,
  selectedUserId,
  onClickAvatar,
}) => {
  const theme = useTheme();
  return (
    <SessionModal open={isOpened} justifyContent="stretch">
      <>
        <Stack width="100%" direction="row" justifyContent="center" alignItems="center">
          <Typography variant="h4" alignSelf="center" ml="auto">마음에 드는 상대를 골라주세요!</Typography>
          <Box ml="auto">
            <Timer duration={15} remainingTime={timer} />
          </Box>
        </Stack>
        <Box p={1} />
        <Box width="100%" height="1px" bgcolor={theme.palette.divider} />
        <Box p={2} />
        <Grid container width="100%" height="100%" spacing={3}>
          {targetUserList?.map((it, idx) => (
            <Grid item key={idx} xs>
              <AvatarProfile
                selected={selectedUserId === it.user_id}
                onClick={() => onClickAvatar(it.user_id)}
                avatarName={it.avatar_name}
                avatarImagePath={it.avatar_image_path}
              />
            </Grid>
          ))}
        </Grid>
      </>
    </SessionModal>
  );
};
