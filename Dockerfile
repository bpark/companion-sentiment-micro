FROM openjdk:8-jre-alpine

ENV JAVA_APP_JAR companion-sentiment-micro-1.0-SNAPSHOT-fat.jar

EXPOSE 5701 54327

COPY target/$JAVA_APP_JAR /app/
RUN chmod 777 /app/

WORKDIR /app/
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics $JAVA_OPTIONS -jar $JAVA_APP_JAR -cluster"]