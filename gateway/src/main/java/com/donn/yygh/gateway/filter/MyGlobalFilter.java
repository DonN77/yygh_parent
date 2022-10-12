package com.donn.yygh.gateway.filter;

import com.google.gson.JsonObject;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/30 21:58
 **/
//@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Override
    //编写过滤规则
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //因为gateway底层是基于webflux，所以请求报文是 ServerHttpRequest类
        //webflux对应ServerHttpRequest，servlet对应HttpServletRequest
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();   //获取请求路径

        //对于登录接口的请求就不要拦截了
        if(antPathMatcher.match("/admin/user/**",path)){
            return chain.filter(exchange);
        }else{  //对于非登录接口，需要验证：是否登录，登录后才能通过
            //如果已经登录，每次请求请求头都会携带 X-Token信息，获取X-Token
            List<String> strings = request.getHeaders().get("X-Token");
            if(strings == null|| strings.size() == 0){   //请求没有携带X-Token，表示还没有登录
            // 拦截：
                ServerHttpResponse response = exchange.getResponse();
                //处理逻辑，进行跳转
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //路由跳转
                response.getHeaders().set(HttpHeaders.LOCATION,"http://localhost:9528");

                //结束请求
                return response.setComplete();
            }else{   //已登录,放行
                return chain.filter(exchange);
            }
        }
    }

    @Override
    //影响自定义过滤器的执行顺序：值越小，优先级越高
    public int getOrder() {
        return 0;
    }

    //没有登录返回的信息
    private Mono<Void> out(ServerHttpResponse response) {
        JsonObject message = new JsonObject();
        message.addProperty("success", false);
        message.addProperty("code", 28004);
        message.addProperty("data", "鉴权失败");
        byte[] bits = message.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //response.setStatusCode(HttpStatus.UNAUTHORIZED);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }
}
