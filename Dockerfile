FROM maven:3.8-openjdk-17 as builder
WORKDIR /app
COPY . .
RUN mvn -q package liberty:copy-dependencies

# ─────────────────────────────────────────
FROM icr.io/appcafe/open-liberty:kernel-slim-java17-openj9-ubi

# 1) Liberty 設定
COPY --chown=1001:0 src/main/liberty/config/ /config/

# 2) JDBC ドライバを「builder」ステージからコピー
COPY --chown=1001:0 --from=builder \
     /app/target/liberty/wlp/usr/shared/resources/mysql/mysql-connector-j-*.jar \
     /opt/ol/shared/resources/

# 3) JDBC 機能をインストール
RUN features.sh --acceptLicense install jdbc-4.3

# 4) WAR を配置
COPY --chown=1001:0 --from=builder /app/target/*.war /config/apps/

RUN configure.sh