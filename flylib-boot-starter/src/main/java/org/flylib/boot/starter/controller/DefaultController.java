package org.flylib.boot.starter.controller;

import org.flylib.boot.starter.exception.UnknownResourceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Default controller that exists to return a proper REST response for unmapped requests.
 */
@Controller
public class DefaultController {

    @RequestMapping("/**")
    public void unmappedRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        throw new UnknownResourceException("Th|ere is no resource for path " + uri);
    }
}
