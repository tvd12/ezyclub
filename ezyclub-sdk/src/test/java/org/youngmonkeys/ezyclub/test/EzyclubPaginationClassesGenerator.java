package org.youngmonkeys.ezyclub.test;

import org.youngmonkeys.devtools.pagination.PaginationClassesGenerator;

public class EzyclubPaginationClassesGenerator {

    public static void main(String[] args) throws Exception {
        new PaginationClassesGenerator(Object.class)
            .generate();
    }
}
