FROM openjdk:8-jre-slim
MAINTAINER JayTan

ENV PARAMS=""

ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD target/easy-task-scheduler-api.jar /app.jar

ENTRYPOINaliyueshuo
T ["sh","-c","java -jar $JAVA_OPTS /app.jar $PARAMS"]