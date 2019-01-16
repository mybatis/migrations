FROM openjdk:8
RUN ["mkdir", "-p", "/opt/migrations"]

ADD ./target/appassembler /opt/migrations

VOLUME ["/migration"]
WORKDIR /migration

ENTRYPOINT ["/opt/migrations/bin/migrate"]
