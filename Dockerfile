FROM maven:3.8-openjdk-17 as builder
WORKDIR /app
COPY . .
RUN mvn package

FROM icr.io/appcafe/open-liberty:kernel-slim-java17-openj9-ubi
COPY --chown=1001:0 /src/main/liberty/config /config

# ローカルの JDBC ドライバをワイルドカード指定でコピー
COPY --chown=1001:0 \
     target/liberty/wlp/usr/shared/resources/mysql/mysql-connector-j-*.jar \
     /opt/ol/shared/resources/

# JDBC 機能をインストール
RUN features.sh --acceptLicense install jdbc-4.3

COPY --chown=1001:0 --from=builder /app/target/*.war /config/apps

RUN configure.sh
