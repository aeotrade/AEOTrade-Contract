package com.aeotrade.chain.contract.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;

public class OAuth2JwtInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getCredentials() instanceof AbstractOAuth2Token)) {
            return;
        }

        AbstractOAuth2Token token = (AbstractOAuth2Token) authentication.getCredentials();

        String extractedToken = String.format("%s %s", OAuth2AccessTokenInterceptor.BEARER.toLowerCase(), token.getTokenValue());
        requestTemplate.header(OAuth2AccessTokenInterceptor.AUTHORIZATION);
        requestTemplate.header(OAuth2AccessTokenInterceptor.AUTHORIZATION, extractedToken);

    }
}
