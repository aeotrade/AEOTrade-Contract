package com.aeotrade.chain.contract.exception;

import com.aeotrade.chain.contract.vo.ResultBean;
import com.aeotrade.chain.contract.vo.StatusEnum;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApplicationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7703523927381670480L;

	private final HttpStatus status;
	
	private final ResultBean<?> result;
	
	public ApplicationException(HttpStatus httpStatus, ResultBean resultBean){
		this.result=resultBean;
		this.status=httpStatus;
	}
	
	public ApplicationException(HttpStatus httpStatus){
		this.status=httpStatus;
		this.result=new ResultBean<>();
		this.result.setMessage("系统异常，请联系客服。");
	}

	public ApplicationException(HttpStatus httpStatus, StatusEnum statusEnum){
		this.result=new ResultBean<>();
		this.result.setStatusEnum(statusEnum);
		this.status=httpStatus;
	}

	public ApplicationException(HttpStatus httpStatus,String code,String message){
		this.status=httpStatus;
		this.result=new ResultBean<>();
		this.result.setCode(code);
		this.result.setMessage(message);
	}

	public ApplicationException(HttpStatus httpStatus,String message){
		this.status=httpStatus;
		this.result=new ResultBean<>();
		this.result.setMessage(message);
	}

}
