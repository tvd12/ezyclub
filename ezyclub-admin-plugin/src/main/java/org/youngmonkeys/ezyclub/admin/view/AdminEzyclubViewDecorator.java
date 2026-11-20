package org.youngmonkeys.ezyclub.admin.view;

import com.tvd12.ezyfox.bean.annotation.EzySingleton;
import com.tvd12.ezyhttp.server.core.view.View;
import com.tvd12.ezyhttp.server.core.view.ViewDecorator;
import org.youngmonkeys.ezyplatform.annotation.AdminId;

import javax.servlet.http.HttpServletRequest;

import static org.youngmonkeys.ezyplatform.constant.CommonConstants.VIEW_VARIABLE_ADDITIONAL_STYLE_FILES;

@EzySingleton
public class AdminEzyclubViewDecorator implements ViewDecorator {

    @Override
    public void decorate(HttpServletRequest request, View view) {
        Long adminId = (Long) request.getAttribute(
            AdminId.class.getName()
        );
        if (adminId == null) {
            return;
        }
        view.appendValueToVariable(
            VIEW_VARIABLE_ADDITIONAL_STYLE_FILES,
            "/ezyclub/css/style.css"
        );
    }
}
