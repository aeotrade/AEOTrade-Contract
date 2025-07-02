package com.aeotrade.chain.contract.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@UtilityClass
@Slf4j
public class UserUtil {
    private ObjectMapper mapper=new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    public Long getUserId(){
        String userInfo=((Jwt)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getClaimAsString("user_name");
        try {
            LoginUser loginUser = mapper.readValue(userInfo, LoginUser.class);
            return loginUser.getStaffId();
        } catch (JsonProcessingException e) {
            log.error("解析用户信息时出错。",e);
        }
        return null;
    }
}
