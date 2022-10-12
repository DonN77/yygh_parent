package com.donn.yygh.common.utils;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

public class JwtHelper {
    private static long tokenExpiration = 24*60*60*1000;    //一天
    private static String tokenSignKey = "123456";   //盐值

    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()
                .setSubject("YYGH-USER")  //设置主题，可以不设置，没有影响
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)) //设置token过期时间(毫秒)，到当前时间 + tokenExpiration 就过期
                .claim("userId", userId)   //如果怕用户信息泄露claim 这两步可以弄复杂一点，可以对userId、userName加时间戳之类的
                .claim("userName", userName)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)  //设置加密算法 和 盐值
                .compressWith(CompressionCodecs.GZIP)   //进行压缩
                .compact();
        return token;
    }

    public static Long getUserId(String token) {
        if(StringUtils.isEmpty(token)) return null;
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);  //通过盐值进行解析，解析出用户信息
        Claims claims = claimsJws.getBody();
        Integer userId = (Integer)claims.get("userId");
        return userId.longValue();
    }

    public static String getUserName(String token) {
        if(StringUtils.isEmpty(token)) return "";
        Jws<Claims> claimsJws
                = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String)claims.get("userName");
    }
    
    public static void main(String[] args) {
//        String token = JwtHelper.createToken(1L, "55");
//        System.out.println(token);
        System.out.println(JwtHelper.getUserId("eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJSiox099ANDXYNUtJRSq0oULIyNDMzNTK0tDQw1FEqLU4t8kxRsjI2gbD9EnNTgXpclGoBzmGOFT8AAAA.0EZ-xE-EBesz900PqE19OilNEU3dbcJxn81jCYy0tlFM1hvzGaNAFI8grTF68KIa3VJuj8X3uPGX2mY-COWqxw"));
//        System.out.println(JwtHelper.getUserName(token));
    }
}