FROM kshivaprasad/java:latest

RUN   mkdir -p /etc/dms/financialstatements

WORKDIR /etc/dms/financialstatements

ADD financialstatements/build/libs/*.jar .

CMD java $JAVA_OPTS  -jar *.jar

EXPOSE 8080
