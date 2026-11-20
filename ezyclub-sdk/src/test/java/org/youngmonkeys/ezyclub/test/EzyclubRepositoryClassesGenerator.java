package org.youngmonkeys.ezyclub.test;

import org.youngmonkeys.devtools.repository.RepositoryClassesGenerator;

public class EzyclubRepositoryClassesGenerator {

    public static void main(String[] args) throws Exception {
        new RepositoryClassesGenerator(Object.class)
            .generate();
    }
}
