#
#    Copyright 2010-2020 the original author or authors.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

FROM openjdk:8 AS build
RUN mkdir -p /opt/migrations/build
WORKDIR /opt/migrations/build
COPY . .
RUN ./mvnw package -DskipTests

FROM openjdk:8
RUN ["mkdir", "-p", "/opt/migrations"]

COPY --from=build /opt/migrations/build/target/appassembler /opt/migrations

VOLUME ["/migration"]
WORKDIR /migration

ENTRYPOINT ["/opt/migrations/bin/migrate"]
