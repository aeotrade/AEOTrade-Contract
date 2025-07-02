package com.aeotrade.chain.contract.controller;

import com.aeotrade.chain.contract.exception.ApplicationException;
import com.aeotrade.chain.contract.vo.ResultBean;
import com.aeotrade.chain.contract.vo.StatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.Collection;

@Controller
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(value = AccessDeniedException.class)
	public ResponseEntity<ResultBean<String>> accessDeniedException(AccessDeniedException exception){

		ResultBean<String> resultBean = new ResultBean<>();
		if (exception != null) {
			log.warn("用户访问阻止。",exception);
			resultBean.setMessage("用户访问禁止");
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resultBean);
	}

	@ExceptionHandler(value = ApplicationException.class)
	public ResponseEntity<ResultBean> applicationException(ApplicationException exception){
		return ResponseEntity.status(exception.getStatus()).body(exception.getResult());
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ResultBean<String>> defaultExceptionHandler(HttpServletRequest req, Exception e) {
		log.error("---DefaultException Handler---Host {} invokes url {} ERROR: {}", req.getRemoteHost(),
				req.getRequestURL(), e.getMessage());
		log.error("exception:", e);
		ResultBean<String> resultBean = new ResultBean<>();
		resultBean.setMessage("系统出现异常，请联系客服。");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultBean);
	}

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public ResponseEntity<ResultBean<String>> methodArgumentNotValidHandler(MethodArgumentNotValidException exception){
		ResultBean<String> resultBean = new ResultBean<>();
		resultBean.setMessage(this.formBindMessage(exception.getBindingResult().getFieldErrors()));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultBean);
	}

	@ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ResultBean<String>> methodArgumentTypeMismatchHandler()  {
		ResultBean<String> resultBean = new ResultBean<>();
		resultBean.setStatusEnum(StatusEnum.PARAMETER_DISCREPANCY);
		resultBean.setMessage("方法参数有误");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultBean);
	}

	@ExceptionHandler(value = HttpMessageNotReadableException.class)
	public ResponseEntity<ResultBean<String>> httpMessageNotReadableExceptionHandler()  {
		ResultBean<String> resultBean = new ResultBean<>();
		resultBean.setStatusEnum(StatusEnum.PARAMETER_DISCREPANCY);
		resultBean.setMessage("数据格式不符合规范");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultBean);
	}


	@ExceptionHandler(value = MissingServletRequestParameterException.class)
	public ResponseEntity<ResultBean<String>> missingServletRequestParameterHandler() {
		ResultBean<String> resultBean = new ResultBean<>();
		resultBean.setStatusEnum(StatusEnum.PARAMETER_EXCEPTION);
		resultBean.setMessage("缺少必要参数！");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultBean);
	}

	private String formBindMessage(Collection<FieldError> errors) {
		if (CollectionUtils.isEmpty(errors)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();

		for (ObjectError error : errors) {
			sb.append(error.getDefaultMessage());
			sb.append("\r\n");
		}
		return sb.toString();

	}
}
