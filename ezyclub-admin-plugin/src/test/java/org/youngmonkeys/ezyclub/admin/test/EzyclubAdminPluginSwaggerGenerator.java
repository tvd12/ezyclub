package org.youngmonkeys.ezyclub.admin.test;

import org.youngmonkeys.devtools.swagger.SwaggerGenerator;

import java.io.File;

public class EzyclubAdminPluginSwaggerGenerator {

    public static void main(String[] args) throws Exception {
        SwaggerGenerator swaggerGenerator = new SwaggerGenerator(
            "org.youngmonkeys.ezyclub.admin.controller"
        );
        String filePath = "src/test/resources/static/files/swagger.yaml";
        File srcFolder = new File("src");
        if (!srcFolder.exists()) {
            filePath = "ezyclub-admin-plugin/" + filePath;
        }
        swaggerGenerator.generateToFile(filePath);
    }
}
