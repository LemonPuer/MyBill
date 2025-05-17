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

    @Value("${JWT.refresh.tokenKey}")
    private String tokenKey;

    @Value("${JWT.refresh.tokenExpireDay}")
    private Integer tokenExpireDay;
}
