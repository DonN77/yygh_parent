package com.donn.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.donn.yygh.common.exception.YyghException;
import com.donn.yygh.common.utils.JwtHelper;
import com.donn.yygh.enums.AuthStatusEnum;
import com.donn.yygh.enums.StatusEnum;
import com.donn.yygh.model.user.Patient;
import com.donn.yygh.model.user.UserInfo;
import com.donn.yygh.user.mapper.UserInfoMapper;
import com.donn.yygh.user.service.PatientService;
import com.donn.yygh.user.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donn.yygh.vo.user.LoginVo;
import com.donn.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author donn
 * @since 2022-10-03
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PatientService patientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //1.先获取用户输入的手机号和验证码
        String code = loginVo.getCode();
        String phone = loginVo.getPhone();
        //2.判断手机号和验证码是否为空
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(phone)){
            throw new YyghException(20001,"手机号或者验证码不能为空");
        }
        //3.对验证码做进一步确认
        String redisCode =(String) redisTemplate.opsForValue().get(phone);
        if (StringUtils.isEmpty(redisCode) || !redisCode.equals(code)){
            throw new YyghException(20001,"验证码有误");
        }

        String openid = loginVo.getOpenid();
        UserInfo userInfo = null;
        UserInfo phoneInfo = null;
        //判断openid是否为空
        //因为 微信强制绑定手机号，与使用手机号登录 使用的是同一个页面
        //所以 需要判断 是否携带了 openid
        if (StringUtils.isEmpty(openid)){  //openid为空，表示纯手机号登录

            //4.判断该手机号是否是第一次注册，是的话要注册到数据库表中
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone",phone);
            userInfo = baseMapper.selectOne(queryWrapper);
            if (userInfo == null){
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                baseMapper.insert(userInfo);
                userInfo.setStatus(1);
            }
        }else{   //openid不为空，表示微信强制绑定手机号，只有首次使用微信登录并且强制绑定手机号的时候会走这个else
            //查询用户有没有使用该phone登录过
            QueryWrapper<UserInfo> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("phone",loginVo.getPhone());
            phoneInfo = baseMapper.selectOne(phoneWrapper);

            //查询使用微信登录的表记录
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("openid",openid);
            userInfo = baseMapper.selectOne(queryWrapper);

            if (phoneInfo == null){   //首次微信登录之前，没有使用手机号登录过
                userInfo.setPhone(loginVo.getPhone());
                baseMapper.updateById(userInfo);
            }else{   //首次微信登录之前，使用手机号登录过
                //因为用户如果第一次就使用手机号登录，可能已经把身份认证等操作做了，数据库中已经有值了
                //而微信登录只是首次登陆，只会在表中存 openid 和 nickname
                //所以需要把 使用openid查询的openid、nickname值，放到 phone查询的对象中
                phoneInfo.setOpenid(userInfo.getOpenid());
                phoneInfo.setNickName(userInfo.getNickName());

                //更新完后，写回
                baseMapper.updateById(phoneInfo);

                //删除微信初次登录的表记录（只有openid和nickname的那条表记录）
                baseMapper.deleteById(userInfo.getId());
                //因为生成 token 需要userInfo的id，所有需要把 phoneInfo的id给userInfo，因为只保留下数据表中phoneInfo记录
                userInfo.setId(phoneInfo.getId());
            }
        }

        //5.验证用户的status
        if(userInfo.getStatus() == 0 || (phoneInfo != null && phoneInfo.getStatus() == 0)){
            throw new YyghException(20001,"用户锁定中");
        }

        //6.返回用户信息
        Map<String,Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)){
            name = userInfo.getPhone();
        }
        map.put("name",name);

        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token",token);
        return map;
    }

    @Override
    public UserInfo getUserInfo(Long userId) {
        UserInfo userInfo = baseMapper.selectById(userId);
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        return userInfo;
    }

    @Override
    public Page<UserInfo> getUserInfoPage(Integer pageNum, Integer pageSize, UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> page = new Page<>(pageNum,pageSize);
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(userInfoQueryVo.getKeyword())){
            queryWrapper.like("name",userInfoQueryVo.getKeyword()).or().eq("phone",userInfoQueryVo.getKeyword());
        }
        if (!StringUtils.isEmpty(userInfoQueryVo.getStatus())){
            queryWrapper.eq("status",userInfoQueryVo.getStatus());
        }
        if (!StringUtils.isEmpty(userInfoQueryVo.getAuthStatus())){
            queryWrapper.eq("auth_status",userInfoQueryVo.getAuthStatus());
        }
        if (!StringUtils.isEmpty(userInfoQueryVo.getCreateTimeBegin())){
            queryWrapper.ge("create_time",userInfoQueryVo.getCreateTimeBegin());
        }
        if (!StringUtils.isEmpty(userInfoQueryVo.getCreateTimeEnd())){
            queryWrapper.le("update_time",userInfoQueryVo.getCreateTimeEnd());
        }

        Page<UserInfo> infoPage = baseMapper.selectPage(page, queryWrapper);

        infoPage.getRecords().stream().forEach(item->{
            this.packageUserInfo(item);
        });

        return infoPage;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        //mybatis-plus支持 不覆盖式的修改，传入的对象哪些字段有值就只修改那些字段
        if (status == 1 || status == 0){
            UserInfo userInfo = new UserInfo();
            userInfo.setId(id);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> detail(Long id) {
        UserInfo userInfo = baseMapper.selectById(id);

        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",id);
        List<Patient> patients = patientService.selectList(queryWrapper);

        Map<String,Object> map = new HashMap<>(2);
        map.put("patients",patients);
        map.put("userInfo",userInfo);
        return map;
    }

    //封装UserInfo的详细认证信息，因为数据表中一些数据存储只是数值类型，具体内容需要映射
    private void packageUserInfo(UserInfo item) {
        Integer authStatus = item.getAuthStatus();
        Integer status = item.getStatus();
        item.getParam().put("statusString", StatusEnum.getStatusStringByStatus(status));
        item.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(authStatus));
    }
}
