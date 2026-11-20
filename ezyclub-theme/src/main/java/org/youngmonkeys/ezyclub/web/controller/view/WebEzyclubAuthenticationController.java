package org.youngmonkeys.ezyclub.web.controller.view;

import com.tvd12.ezyhttp.server.core.annotation.Authenticatable;
import com.tvd12.ezyhttp.server.core.annotation.Controller;
import com.tvd12.ezyhttp.server.core.annotation.DoGet;
import com.tvd12.ezyhttp.server.core.view.Redirect;
import lombok.AllArgsConstructor;
import org.youngmonkeys.ezyclub.web.view.WebEzyclubPageViewFactory;
import org.youngmonkeys.ezyplatform.web.annotation.UserId;

@Authenticatable
@Controller
@AllArgsConstructor
public class WebEzyclubAuthenticationController {

    private final WebEzyclubPageViewFactory pageViewFactory;

    @DoGet("/login")
    public Object loginGet(@UserId Long userId) {
        if (userId != null && userId > 0) {
            return Redirect.to("/");
        }
        return pageViewFactory.newHomeViewBuilder()
            .addVariable("showLogin", Boolean.TRUE)
            .build();
    }

    @DoGet("/register")
    public Object registerGet(@UserId Long userId) {
        if (userId != null && userId > 0) {
            return Redirect.to("/");
        }
        return pageViewFactory.newHomeViewBuilder()
            .addVariable("showRegister", Boolean.TRUE)
            .build();
    }
}
