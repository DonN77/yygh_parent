package com.donn.yygh.hosp.controller.admin;

import com.donn.yygh.common.result.R;
import com.donn.yygh.model.acl.User;
import org.springframework.web.bind.annotation.*;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/22 23:43
 **/
@RestController
@RequestMapping("/admin/user")
public class UserController {

    @PostMapping("/login")
    public R login(@RequestBody User user){
        return R.ok().data("token","admin-token");
    }

    @GetMapping("/info")
    public R info(String token){
        return R.ok().data("roles","[admin]")
                    .data("introduction","I am a super administrator")
                    .data("avatar","https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fhbimg.huabanimg.com%2Fe0a25a7cab0d7c2431978726971d61720732728a315ae-57EskW_fw658&refer=http%3A%2F%2Fhbimg.huabanimg.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1666454161&t=a6d61edce53977d1d3ded348f696fb28")
                    .data("name","Super Admin");
    }
}
