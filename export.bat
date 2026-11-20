mvn -pl . clean install & ^
mvn -pl ezyclub-sdk clean install & ^
mvn -pl ezyclub-admin-plugin clean install -Pexport,\!test & ^
mvn -pl ezyclub-web-plugin clean install -Pexport,\!test & ^
mvn -pl ezyclub-theme clean install -Pexport,\!test & ^
ezy.bat package & ^
ezy.bat export
