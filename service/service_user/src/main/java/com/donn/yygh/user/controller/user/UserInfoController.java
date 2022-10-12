package com.donn.yygh.user.controller.user;


import com.donn.yygh.common.result.R;
import com.donn.yygh.common.utils.JwtHelper;
import com.donn.yygh.enums.AuthStatusEnum;
import com.donn.yygh.model.user.UserInfo;
import com.donn.yygh.user.service.UserInfoService;
import com.donn.yygh.vo.user.LoginVo;
import com.donn.yygh.vo.user.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author donn
 * @since 2022-10-03
 */
@RestController
@RequestMapping("/user/userInfo")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo){
        Map<String,Object> map = userInfoService.login(loginVo);
        return R.ok().data(map);
    }

    @GetMapping("/info")
    public R getUserInfo(@RequestHeader String token){
        Long userId = JwtHelper.getUserId(token);
        UserInfo info = userInfoService.getUserInfo(userId);
        return R.ok().data("user",info);
    }

    @PutMapping("/update")
    public R save(@RequestHeader String token, UserAuthVo authVo){
        Long userId = JwtHelper.getUserId(token);
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setName(authVo.getName());
        userInfo.setCertificatesType(authVo.getCertificatesType());
        userInfo.setCertificatesNo(authVo.getCertificatesNo());
        userInfo.setCertificatesUrl(authVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());

        userInfoService.updateById(userInfo);
        return R.ok();
    }

}

