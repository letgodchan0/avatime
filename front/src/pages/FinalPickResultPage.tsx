import React, { FC, useState } from "react";
import { Backdrop, Box, Grid, Typography, useTheme, CircularProgress } from "@mui/material";
import { grey } from "@mui/material/colors";
import { useSelector, useDispatch } from "react-redux";
import Xarrow, { Xwrapper } from "react-xarrows";
import { FinalPickResultRes } from "../apis/response/sessionRes";
import useTimer from "../hooks/useTimer";
import { useEffect } from "react";
import { SessionModal } from "../components/session/modal/SessionModal";
import { useNavigate } from "react-router";
import { setSubMeetingRoomId } from "../stores/slices/meetingSlice";
import { AvatimeApi } from "../apis/avatimeApi";

interface IProps {}

export const FinalPickResultPage: FC<IProps> = (props) => {
  const navigate = useNavigate();
  const roomId = useSelector((state: any) => state.meeting.roomId);
  const gender = useSelector((state: any) => state.user.userGender);
  const userId = useSelector((state: any) => state.user.userId);
  const [pickResult, setPickResult] = useState<FinalPickResultRes>();

  useEffect(() => {
    if (!roomId || !gender || !userId) {
      return;
    }

    AvatimeApi.getInstance().getFinalPickResult({
      meetingroom_id: roomId,
      user_id: userId,
    }, {
      onSuccess: (data) => {
        data.result_list.sort((a: any) => (a.gender === gender ? -1 : 1));
        setPickResult(data);
      },
      navigate,
    })
  }, [gender, navigate, roomId, userId]);

  const timer = useTimer(5, 1000);
  const [arrowOrderList, setArrowOrderList] = useState<number[]>([]);

  useEffect(() => {
    if (!pickResult) {
      return;
    }

    let idx = 1;
    let cnt = 1;
    let prevUserIndex = 0;
    const temp = Array(pickResult.result_list.length).fill(-1);
    temp[0] = 0;

    while (cnt < pickResult.result_list.length) {
      const pickUserIndex = pickResult.result_list.findIndex(
        // eslint-disable-next-line no-loop-func
        (it) => it.id === pickResult.result_list[prevUserIndex].pick_user_id
      );
      if (temp[pickUserIndex] !== -1) {
        while (temp[idx] !== -1 && idx < pickResult.result_list.length) {
          idx++;
        }
        temp[idx] = cnt++;
        prevUserIndex = idx++;
      } else {
        temp[pickUserIndex] = cnt++;
        prevUserIndex = pickUserIndex;
      }
    }
    setArrowOrderList(temp);
  }, [pickResult]);

  const dispatch = useDispatch();
  const onModalClose = () => {
    if (pickResult?.matched) {
      dispatch(setSubMeetingRoomId(pickResult.meetingroom_id));
      navigate("/subSession", { replace: true });
    } else {
      navigate("/main", { replace: true });
    }
  };

  if (!pickResult) {
    return <CircularProgress />;
  }

  return (
    <FinalPickResultPagePresenter
      pickResult={pickResult}
      timer={timer}
      arrowOrderList={arrowOrderList}
      onModalClose={onModalClose}
      gender={gender}
    />
  );
};

interface IPresenterProps {
  pickResult: FinalPickResultRes;
  timer: number;
  arrowOrderList: number[];
  onModalClose: () => void;
  gender: string;
}

const FinalPickResultPagePresenter: FC<IPresenterProps> = ({
  pickResult,
  timer,
  arrowOrderList,
  onModalClose,
  gender,
}) => {
  const theme = useTheme();

  const UserProfileList = (left: boolean) => (
    <Grid container item spacing={2} direction="column" xs={2} justifyContent="center">
      {pickResult.result_list
        .filter((it) => (left ? gender === it.gender : gender !== it.gender))
        .map((it) => (
          <Grid
            item
            xs={3}
            key={it.avatar_id}
            position="relative"
            display="flex"
            flexDirection="column"
            alignItems="center"
          >
            <Box
              position="relative"
              sx={{
                height: "70%",
                aspectRatio: "auto 1 / 1",
                backgroundColor: "white",
                backgroundImage: `url(${it.avatar_image_path})`,
                backgroundSize: "cover",
                backgroundPosition: "center 50%",
                borderRadius: "100%",
                border: `3px solid ${
                  it.gender === "M" ? theme.palette.primary.light : theme.palette.error.light
                }`,
              }}
            >
              <Box
                id={(left ? "in" : "out") + it.id}
                position="absolute"
                top="20%"
                right={left ? 0 : 100}
                left={left ? 100 : 0}
              />
              <Box
                id={(left ? "out" : "in") + it.id}
                position="absolute"
                bottom="20%"
                right={left ? 0 : 100}
                left={left ? 100 : 0}
              />
            </Box>
            <Typography variant="subtitle1">{it.avatar_name}</Typography>
          </Grid>
        ))}
    </Grid>
  );

  const headCount = pickResult.result_list.length;

  return (
    <Box height="100vh" display="flex" alignItems="stretch" justifyContent="stretch">
      <Grid
        container
        borderRadius="10px"
        m={3}
        p={2}
        bgcolor={grey[200]}
        direction="row"
        spacing={3}
      >
        {UserProfileList(true)}
        <Grid item xs />
        {UserProfileList(false)}
        <Xwrapper>
          {pickResult.result_list.map((it, idx) => {
            const color =
              headCount <= -timer &&
              it.id === pickResult.result_list?.find((i) => i.id === it.pick_user_id)?.pick_user_id
                ? "red"
                : theme.palette.primary.main;
            return (
              arrowOrderList[idx] <= -timer && (
                <Xarrow
                  key={it.id}
                  start={"out" + it.id}
                  end={"in" + it.pick_user_id}
                  curveness={0}
                  lineColor={color}
                  headColor={color}
                  animateDrawing={0.5}
                />
              )
            );
          })}
        </Xwrapper>
      </Grid>
      <Backdrop open={0 < timer}>
        <Typography variant="h1" color="white">
          {timer}
        </Typography>
      </Backdrop>
      <SessionModal open={timer <= -headCount - 1} justifyContent="center" onClose={onModalClose}>
        <Typography variant="h3" textAlign="center">
          {pickResult.matched
            ? (<>{"매칭에 성공하셨어요!!"}<br/>{"클릭 시, 가면이 벗겨지고 둘만의 시간을 갖게 돼요 >.<"}</>)
            : "힝ㅠㅠ 다음 인연을 찾아보세요!"}
        </Typography>
      </SessionModal>
    </Box>
  );
};
