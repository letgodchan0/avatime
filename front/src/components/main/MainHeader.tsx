import React, { FC, useContext, useState } from "react";
import Avatar from "@mui/material/Avatar";
import logo from "../../assets/avartimeLogo.png";
import { Box, IconButton, Menu, MenuItem, Slider, Stack, useTheme } from "@mui/material";
import { Link } from "react-router-dom";
import Tooltip from "@mui/material/Tooltip";
import "../../style.css";
import { useSelector, useDispatch } from "react-redux";
import { useNavigate } from "react-router";
import { reset, setUserName, setUserDesc, setIsLogin } from "../../stores/slices/userSlice";
import { AvatimeApi } from "../../apis/avatimeApi";
import { AvatimeWs } from "../../apis/avatimeWs";
import { resetMeeting } from "../../stores/slices/meetingSlice";
import { resetWaiting } from "../../stores/slices/waitingSlice";
import { VolumeController } from "../VolumeController";
import { SoundIconButton } from "../SoundButton";
import { useSound } from "../../hooks/useSound";
import { AlertSnackbar } from "../AlertSnackbar";

interface IProps {
  hideSettings?: boolean;
}

/**
 * @author
 * @function @MainHeader
 **/

export const MainHeader: FC<IProps> = ({ hideSettings = false }) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [anchorElUser, setAnchorElUser] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorElUser);
  const profileImagePath = useSelector((state: any) => state.user.profileImagePath);

  const handleOpenUserMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElUser(event.currentTarget);
  };

  const handleCloseUserMenu = () => {
    setAnchorElUser(null);
  };

  const [showSnack, setShowSnack] = useState(false);

  const logout = () => {
    localStorage.clear();
    dispatch(reset());
    dispatch(resetWaiting());
    dispatch(resetMeeting());
    dispatch(setUserName(""));
    dispatch(setUserDesc(""));
    dispatch(setIsLogin(false));
    AvatimeApi.getInstance().logout();
    AvatimeWs.getInstance().logout();
    setShowSnack(true);
    navigate("/");
  };

  const theme = useTheme();
  const ref = useSound();

  return (
    <>
      <Box display="flex" justifyContent="center" alignItems="center" marginBottom="2%">
        <Link
          ref={ref}
          to="/main"
          style={{
            display: "flex",
            justifyContent: "center",
          }}
        >
          <img src={logo} alt="??????" style={{ width: "70%", paddingTop: "2%" }} />
        </Link>

        {!hideSettings && (
          <Stack direction="row" position="absolute" right="80px">
            <Stack width="100px" justifyContent="center">
              <VolumeController />
            </Stack>
              <SoundIconButton
                onClick={handleOpenUserMenu}
                style={{ marginLeft: "27%", marginRight: "3%" }}
              >
            <Tooltip title="??????">
                <Avatar
                  alt="????????? ??????"
                  src={profileImagePath}
                  sx={{ width: 56, height: 56 }}
                  aria-controls={open ? "composition-menu" : undefined}
                  aria-expanded={open ? "true" : undefined}
                />
            </Tooltip>
              </SoundIconButton>
            <Menu
              sx={{
                mt: "60px",
                justifyContent: "center",
                flexDirection: "center",
                textAlign: "center",
              }}
              id="profilemenu"
              anchorEl={anchorElUser}
              anchorOrigin={{
                vertical: "top",
                horizontal: "right",
              }}
              keepMounted
              transformOrigin={{
                vertical: "top",
                horizontal: "right",
              }}
              open={open}
              onClose={handleCloseUserMenu}
            >
              <MenuItem sx={{ display: "flex", flexDirection: "center", justifyContent: "center" }}>
                <Link to="/mypage" style={{ textDecoration: "none", color: "black" }}>
                  ???????????????
                </Link>
              </MenuItem>
              <MenuItem sx={{ flexDirection: "center", display: "flex", justifyContent: "center" }}>
                <Link to="/canvas" style={{ textDecoration: "none", color: "black" }}>
                  ????????????
                </Link>
              </MenuItem>
              <MenuItem
                onClick={() => setShowSnack(true)}
                style={{ color: "black" }}
                sx={{ display: "flex", flexDirection: "center", justifyContent: "center" }}
              >
                <p style={{ color: theme.palette.error.main }}>????????????</p>
              </MenuItem>
            </Menu>
          </Stack>
        )}
      </Box>
      <AlertSnackbar
        open={showSnack}
        onClose={() => setShowSnack(false)}
        message="?????? ???????????? ????????????????"
        alertColor="warning"
        type="confirm"
        onSuccess={logout}
      />
    </>
  );
};
