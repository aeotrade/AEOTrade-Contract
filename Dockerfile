FROM hub.aeotrade.com/open-source/openjdk:17.0-jdk-slim
WORKDIR /usr/share/aeochaincontract

COPY ./target/aeochaincontract.war .

RUN useradd -m aeotrade -s /bin/bash && \
    chown -R aeotrade:aeotrade /usr/share/aeochaincontract && \
    chmod +x /usr/share/aeochaincontract/aeochaincontract.war

USER aeotrade
EXPOSE 8081
CMD ["java", "-jar", "aeochaincontract.jar"]