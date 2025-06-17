# ---------- build stage ----------
FROM maven:3.8-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
# JDBC ドライバ取得とビルド
RUN mvn -Dmaven.test.skip=true package \
 && mvn dependency:copy \
      -Dartifact=com.mysql:mysql-connector-j:9.3.0 \
      -DoutputDirectory=target/mysql

# ---------- runtime stage ----------
FROM icr.io/appcafe/open-liberty:kernel-slim-java17-openj9-ubi

# JDBC ドライバ
COPY --chown=1001:0 --from=builder /app/target/mysql/mysql-connector-j-9.3.0.jar \
      /opt/ol/wlp/usr/shared/resources/mysql/

# サーバー設定 (server.xml, jvm.options など)
COPY --chown=1001:0 src/main/liberty/config/ /config/

# 必要な Liberty 機能をインストール
RUN features.sh

# アプリケーション WAR
COPY --chown=1001:0 --from=builder /app/target/*.war /config/apps/

# キャッシュ作成・最終調整
RUN configure.sh
