package org.youngmonkeys.ezyclub.web.test;

import com.tvd12.ezyhttp.server.boot.EzyHttpApplicationBootstrap;
import com.tvd12.ezyhttp.server.core.annotation.ComponentsScan;
import com.tvd12.ezyhttp.server.core.annotation.PropertiesSources;

@PropertiesSources({
    "config.properties",
    "setup.properties"
})
@ComponentsScan({
    "org.youngmonkeys.ezyplatform",
    "org.youngmonkeys.ezyclub",
    "org.youngmonkeys.ezylogin",
    "org.youngmonkeys.ezymail",
    "org.youngmonkeys.ezyarticle",
    "org.youngmonkeys.ezycrm",
    "org.youngmonkeys.ezyrating"
})
public class EzyclubThemeStartupTest {

    public static void main(String[] args) throws Exception {
        EzyHttpApplicationBootstrap.start(EzyclubThemeStartupTest.class);
    }
}
