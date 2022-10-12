package com.donn.yygh.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.donn.yygh.model.user.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.donn.yygh.vo.user.LoginVo;
import com.donn.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author donn
 * @since 2022-10-03
 */
public interface UserInfoService extends IService<UserInfo> {

    Map<String, Object> login(LoginVo loginVo);

    UserInfo getUserInfo(Long userId);

    Page<UserInfo> getUserInfoPage(Integer pageNum, Integer pageSize, UserInfoQueryVo userInfoQueryVo);

    void updateStatus(Long id, Integer status);

    Map<String, Object> detail(Long id);
}
