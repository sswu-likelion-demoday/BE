package com.sujeongring.domain.chat.controller.advice;

import com.sujeongring.domain.chat.controller.ChatWebSocketController;
import com.sujeongring.global.common.ApiResponse;
import com.sujeongring.global.error.CommonErrorCode;
import com.sujeongring.global.error.ErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice(assignableTypes = ChatWebSocketController.class)
public class ChatWebSocketExceptionHandler {

    @MessageExceptionHandler(BaseException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public ApiResponse<Void> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ApiResponse.error(
                errorCode,
                e.getMessage()
        );
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public ApiResponse<Void> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(
                        CommonErrorCode.INVALID_INPUT_VALUE.getMessage()
                );

        return ApiResponse.error(
                CommonErrorCode.INVALID_INPUT_VALUE,
                message
        );
    }

    @MessageExceptionHandler(MessageConversionException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public ApiResponse<Void> handleMessageConversionException(
            MessageConversionException e
    ) {
        return ApiResponse.error(
                CommonErrorCode.INVALID_REQUEST_BODY
        );
    }
}
