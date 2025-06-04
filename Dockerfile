# ───────────────────────────────────
# 1) ビルド用ステージ：maven で WAR を作る
# ───────────────────────────────────
FROM maven:3.8-openjdk-17 AS builder
WORKDIR /app

# プロジェクト全体を/container 内の /app にコピー
COPY . .

# Maven でパッケージング（liberty プラグインが JDBC ドライバを所定ディレクトリにコピーする）
RUN mvn package


# ───────────────────────────────────
# 2) ランタイム用ステージ：Open Liberty イメージ
# ───────────────────────────────────
FROM icr.io/appcafe/open-liberty:kernel-slim-java17-openj9-ubi

# 2-1) Liberty のサーバー設定をコピー
#      ここでは server.xml などが置いてある src/main/liberty/config を /config に配置
COPY --chown=1001:0 src/main/liberty/config /config

# 2-2) JDBC ドライバ JAR を builder ステージから取り出し
#      builder 内では /app/target/liberty/wlp/usr/shared/resources/mysql に JAR があるはず
COPY --chown=1001:0 --from=builder \
     /app/target/liberty/wlp/usr/shared/resources/mysql/mysql-connector-j-*.jar \
     /opt/ol/shared/resources/

# 2-3) JDBC 機能をインストール（DATA SOURCE 用モジュール）
RUN features.sh --acceptLicense install jdbc-4.3

# 2-4) Maven で作られた WAR をアプリケーションとして配置
COPY --chown=1001:0 --from=builder /app/target/*.war /config/apps

# 2-5) configure.sh を実行して server.xml の設定を反映
RUN configure.sh