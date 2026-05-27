#!/bin/bash
echo "🚀 Starting CorpCare with H2 (offline/local)..."
export SPRING_DATASOURCE_URL='jdbc:h2:file:./data/corpcare;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE'
export SPRING_DATASOURCE_DRIVER='org.h2.Driver'
export SPRING_DATASOURCE_USERNAME='sa'
export SPRING_DATASOURCE_PASSWORD=''
export SPRING_DATASOURCE_DIALECT='org.hibernate.dialect.H2Dialect'
mvn spring-boot:run
