package org.youngmonkeys.ezyclub.web.test;

import org.youngmonkeys.devtools.swagger.SwaggerGenerator;

import java.io.File;

public class EzyclubWebPluginSwaggerGenerator {

    public static void main(String[] args) throws Exception {
        SwaggerGenerator swaggerGenerator = new SwaggerGenerator(
            "org.youngmonkeys.ezyclub.web.controller"
        );
        String filePath = "src/test/resources/static/files/swagger.yaml";
        File srcFolder = new File("src");
        if (!srcFolder.exists()) {
            filePath = "ezyclub-web-plugin/" + filePath;
        }
        swaggerGenerator.generateToFile(filePath);
    }
}
