package com.aeotrade.chain.contract.vo;

import lombok.Data;

@Data
public final class ResultBean<T> {
	 private String code="-999001";
	 private String message;
	 private T data;

	 public ResultBean(){

	 }

	 public ResultBean(StatusEnum statusEnum){
		this.setStatusEnum(statusEnum);
	 }

	 public void setStatusEnum(StatusEnum statusEnum){
		 this.code=statusEnum.getCode();
		 this.message=statusEnum.getMsg();
	 }
}
