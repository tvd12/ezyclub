package org.youngmonkeys.ezyclub.web.test;

import org.youngmonkeys.devtools.swagger.SwaggerGenerator;

import java.io.File;

public class EzyclubThemeSwaggerGenerator {

    public static void main(String[] args) throws Exception {
        SwaggerGenerator swaggerGenerator = new SwaggerGenerator(
            "com.example.hello_ezyplatform.web.controller"
        );
        String filePath = "src/test/resources/static/files/swagger.yaml";
        File srcFolder = new File("src");
        if (!srcFolder.exists()) {
            filePath = "ezyclub-theme/" + filePath;
        }
        swaggerGenerator.generateToFile(filePath);
    }
}
