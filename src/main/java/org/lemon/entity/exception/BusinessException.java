package org.lemon.entity.exception;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/01 22:57:20
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 703489719074576565L;

    public BusinessException(String message){
        super(message);
    }
}
