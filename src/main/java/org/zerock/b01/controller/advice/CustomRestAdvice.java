package org.zerock.b01.controller.advice;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;

@RestController
@Log4j2
public class CustomRestAdvice {

    //Rest방식의 컨트롤러는 대부분 Ajax와 같이 눈에 보이지 않는방식으로 서버를 호춣
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    public ResponseEntity<Map<String, String >> handlerBindException(BindException e){
        log.error(e);

        Map<String, String> errorMap = new HashMap<>();
        if(e.hasErrors()){
            BindingResult bindingResult = e.getBindingResult();

            bindingResult.getFieldErrors().forEach(fieldError -> errorMap.put(fieldError.getField(),fieldError.getCode()));
        }
        return ResponseEntity.badRequest().body(errorMap);
    }





}