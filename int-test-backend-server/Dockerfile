FROM openjdk:8u171-jdk-stretch
# Build the artifacts first
# ../gradlew :int-backend-server:assembleDist :int-backend-server:installDist"
ADD ./build/install/int-test-backend-server /
ENV JAVA_OPTS="-DDEBUG"
CMD ["int-test-backend-server", "443", "backend", "true"]
