FROM gradle:8.9-jdk21 AS build
WORKDIR /src
COPY . .
RUN gradle installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/build/install/browser-use-backend/ ./
EXPOSE 8080
CMD ["bin/browser-use-backend"]
