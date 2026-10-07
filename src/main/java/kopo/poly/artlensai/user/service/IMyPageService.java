package kopo.poly.artlensai.user.service;

import kopo.poly.artlensai.user.dto.MyPageStatDTO;

public interface IMyPageService {

    MyPageStatDTO getStat(
            String email
    ) throws Exception;

    int updateNickname(
            String email,
            String nickname
    ) throws Exception;

    int changePassword(
            String email,
            String currentPassword,
            String newPassword
    ) throws Exception;


    int withdraw(
            String email
    ) throws Exception;

}
