package com.donn.yygh.user.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.donn.yygh.common.result.R;
import com.donn.yygh.model.user.UserInfo;
import com.donn.yygh.user.service.UserInfoService;
import com.donn.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/7 23:52
 **/
@RestController
@RequestMapping("/administrator/userInfo")
public class AdminUserInfoController {

    @Autowired
    private UserInfoService userInfoService;


    //修改用户认证状态
    @PutMapping("/auth/{id}/{authStatus}")
    public R approval(@PathVariable Long id,
                          @PathVariable Integer authStatus){
        if (authStatus == 2 || authStatus == -1){
            UserInfo userInfo = new UserInfo();
            userInfo.setId(id);
            userInfo.setAuthStatus(authStatus);
            userInfoService.updateById(userInfo);
        }
        return R.ok();
    }

    //带条件的用户信息分页
    @GetMapping("/{pageNum}/{limit}")
    public R getUserInfoPage(@PathVariable Integer pageNum,
                             @PathVariable Integer limit,
                            UserInfoQueryVo userInfoQueryVo){

        Page<UserInfo> page = userInfoService.getUserInfoPage(pageNum,limit,userInfoQueryVo);
        return R.ok().data("total",page.getTotal()).data("list",page.getRecords());
    }

    //修改用户状态
    @PutMapping("/{id}/{status}")
    public R updateStatus(@PathVariable Long id,
                          @PathVariable Integer status){
        userInfoService.updateStatus(id,status);
        return R.ok();
    }

    //查看用户详情，查看用户以及该用户的所有就诊人信息
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Long id){
        Map<String,Object> map = userInfoService.detail(id);
        return R.ok().data(map);
    }
}
