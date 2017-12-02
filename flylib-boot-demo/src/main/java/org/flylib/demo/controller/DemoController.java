package org.flylib.demo.controller;

import org.flylib.boot.starter.exception.CustomRuntimeException;
import org.flylib.demo.exception.UserException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demo")
public class DemoController {

    @RequestMapping("")
    public String index() throws RuntimeException {
        UserException userException = new UserException();
        CustomRuntimeException cause = new CustomRuntimeException("001", "User not exists");
        userException.initCause(cause);
        throw userException;
    }
}
