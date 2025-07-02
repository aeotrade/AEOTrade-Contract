package com.aeotrade.chain.contract.exception;

import com.aeotrade.chain.contract.vo.ResultBean;
import com.aeotrade.chain.contract.vo.StatusEnum;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class StatusException  extends RuntimeException{
    private static final long serialVersionUID = -7205966145533426564L;
    private final HttpStatus status;

    private final ResultBean<?> result;

    public StatusException(HttpStatus httpStatus, ResultBean resultBean){
        this.result=resultBean;
        this.status=httpStatus;
    }

    public StatusException(HttpStatus httpStatus, StatusEnum statusEnum){
        this.result=new ResultBean<>();
        this.result.setStatusEnum(statusEnum);
        this.status=httpStatus;
    }

    public StatusException(HttpStatus httpStatus,String code,String message){
        this.status=httpStatus;
        this.result=new ResultBean<>();
        this.result.setCode(code);
        this.result.setMessage(message);
    }

    public StatusException(HttpStatus httpStatus,String message){
        this.status=httpStatus;
        this.result=new ResultBean<>();
        this.result.setMessage(message);
    }
}
