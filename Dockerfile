FROM kshivaprasad/java:latest

RUN   mkdir -p /etc/dms/financialstatments

WORKDIR /etc/dms/financialstatments

ADD financialstatmentse/build/libs/*.jar .

CMD java $JAVA_OPTS  -jar *.jar

EXPOSE 8080
