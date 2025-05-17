package org.lemon.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/17 16:22:51
 */
@Getter
@Service
public class PropertiesService {

    @Value("${email.account:${spring.mail.username}}")
    private String myEmailAccount;

    @Value("${JWT.refresh.tokenKey:0196de86-31d5-7634-a8f0-f3db05cd6656}")
    private String tokenKey;

    @Value("${JWT.refresh.tokenExpireDay:15}")
    private Integer tokenExpireDay;
}
