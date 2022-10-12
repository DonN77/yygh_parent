package com.donn.yygh.user.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.donn.yygh.common.exception.YyghException;
import com.donn.yygh.common.result.R;
import com.donn.yygh.common.utils.JwtHelper;
import com.donn.yygh.model.user.UserInfo;
import com.donn.yygh.user.prop.WeixinProperties;
import com.donn.yygh.user.service.UserInfoService;
import com.donn.yygh.user.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/5 23:22
 **/
@Controller
@RequestMapping("/user/userInfo/wx")
public class WeixinController {
    @Autowired
    private WeixinProperties weixinProperties;
    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("/param")
    @ResponseBody
    public R getWeixinLoginParam() throws UnsupportedEncodingException {
        Map<String,Object> map = new HashMap<>();
        //要对返回给前端的 回调地址 进行编码，UTF-8
        //编码主要是对 地址路径中的 / 进行编码，对特殊字符进行编码
        String url = URLEncoder.encode(weixinProperties.getRedirecturl(), "UTF-8");
        map.put("appid",weixinProperties.getAppid());
        map.put("scope","snsapi_login");
        map.put("redirecturl",url);
        map.put("state",System.currentTimeMillis()+"");
        return R.ok().data(map);
    }

    @GetMapping("/callback")   //微信扫码登陆成功后的回调方法
    public String callback(String code, String state) throws Exception {
        StringBuilder builder = new StringBuilder()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String format = String.format(builder.toString(), weixinProperties.getAppid(), weixinProperties.getAppsecret(), code);
        String result = HttpClientUtils.get(format);

        JSONObject jsonObject = JSONObject.parseObject(result);

        //access_token访问微信服务器的一个凭证，登录后的每次请求微信服务器都需要携带access_token
        String accessToken = jsonObject.getString("access_token");
        //openid是扫码的微信用户在微信服务器的唯一标识符
        String openid = jsonObject.getString("openid");
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid",openid);
        UserInfo userInfo = userInfoService.getOne(queryWrapper);
        if (userInfo == null){  //首次使用微信登录，需要在表中保存一下用户信息
            userInfo = new UserInfo();
            userInfo.setOpenid(openid);
            //给微信服务器发送请求获取当前用户信息
            StringBuilder sb = new StringBuilder("https://api.weixin.qq.com/sns/userinfo")
                    .append("?access_token=%s")
                    .append("&openid=%s");
            String format1 = String.format(sb.toString(), accessToken, openid);
            String s = HttpClientUtils.get(format1);
            JSONObject object = JSONObject.parseObject(s);
            String nickname = object.getString("nickname");
            userInfo.setNickName(nickname);
            userInfo.setStatus(1);

            userInfoService.save(userInfo);
        }

        //验证用户的status
        if(userInfo.getStatus() == 0){
            throw new YyghException(20001,"用户锁定中");
        }

        //返回用户信息
        Map<String,String> map = new HashMap<>();

        //判断用户是否绑定手机号，如果没有绑定表示是首次微信登录
        if(StringUtils.isEmpty(userInfo.getPhone())){  //该微信号没有绑定，则强制绑定,openid不是""
            map.put("openid",openid);
        }else{   //手机号不为空，表示已经绑定过了，则openid是""空的
            map.put("openid","");
        }

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

        return "redirect:http://localhost:3000/weixin/callback?token="+map.get("token")+ "&openid="+map.get("openid")+"&name="+URLEncoder.encode(map.get("name"),"utf-8");
    }
}
