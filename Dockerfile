FROM mysql:8.0.33
ENV AUTO_RUN_DIR /docker-entrypoint-initdb.d
ENV INSTALL_DB_SQL init_database.sql
COPY ./$INSTALL_DB_SQL $AUTO_RUN_DIR/
RUN chmod 777 $AUTO_RUN_DIR/$INSTALL_DB_SQL

ENV MYSQL_ROOT_PASSWORD 123456
VOLUME  /usr/local/mysql/log /var/log/mysql
VOLUME /usr/local/mysql/data /var/lib/mysql

EXPOSE 3306 3306
