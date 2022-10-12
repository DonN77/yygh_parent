package com.donn.yygh.common.handler;

import com.donn.yygh.common.exception.YyghException;
import com.donn.yygh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/22 15:46
 **/
@RestControllerAdvice
@Slf4j
//全局异常处理类，@RestControllerAdvice=@ResponseBody + @ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({Exception.class})
    public R handlerException(Exception ex){
        log.error(ex.getMessage());
        return R.error();
    }

    @ExceptionHandler({SQLException.class})
    public R handlerSQLException(SQLException ex){
        log.error(ex.getMessage());
        return R.error().message("SQL异常");
    }

    @ExceptionHandler({ArithmeticException.class})
    public R handlerArithmeticException(ArithmeticException ex){
        log.error(ex.getMessage());
        return R.error().message("数学运算异常");
    }

    @ExceptionHandler({YyghException.class})
    public R handlerYyghException(YyghException ex){
        log.error(ex.getMessage());
        return R.error().message(ex.getMessage()).code(ex.getCode());
    }

}
