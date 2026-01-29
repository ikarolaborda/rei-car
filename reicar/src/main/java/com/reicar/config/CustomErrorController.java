package com.reicar.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Controller
public class CustomErrorController implements ErrorController {

    @Value("${reicar.error.show-details:true}")
    private boolean showErrorDetails;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String errorMessage = "Internal Server Error";

        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            HttpStatus httpStatus = HttpStatus.resolve(statusCode);
            if (httpStatus != null) {
                errorMessage = httpStatus.getReasonPhrase();
            }
        }

        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("status", statusCode);
        model.addAttribute("error", errorMessage);
        model.addAttribute("path", path != null ? path.toString() : "/");
        model.addAttribute("message", message != null ? message.toString() : "");
        model.addAttribute("devMode", showErrorDetails);

        if (showErrorDetails && exception instanceof Throwable ex) {
            model.addAttribute("exceptionType", ex.getClass().getName());
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            model.addAttribute("stackTrace", sw.toString());
        } else {
            model.addAttribute("exceptionType", null);
            model.addAttribute("stackTrace", null);
        }

        return "error";
    }
}
