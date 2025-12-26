package com.bronx.notification.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class RedirectController {

    @GetMapping("/swagger")
    public RedirectView redirectToNewPath() {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/swagger-ui.html");
        return redirectView;
    }

}
