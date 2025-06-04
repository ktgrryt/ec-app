FROM maven:3.8-openjdk-17 as builder
WORKDIR /app
COPY . .
RUN mvn dependency:copy -Dartifact=com.mysql:mysql-connector-j:9.3.0 -DoutputDirectory=/app/target/mysql
RUN mvn package

FROM icr.io/appcafe/open-liberty:kernel-slim-java17-openj9-ubi
RUN mkdir -p /opt/ol/wlp/usr/shared/resources/mysql
COPY --chown=1001:0 --from=builder /app/target/mysql/mysql-connector-j-9.3.0.jar /opt/ol/wlp/usr/shared/resources/mysql/
COPY --chown=1001:0 /src/main/liberty/config /config
RUN features.sh
COPY --chown=1001:0 --from=builder /app/target/*.war /config/apps
RUN configure.sh
