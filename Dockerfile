FROM openjdk:17-oracle
ARG JAR_FILE=build/libs/Habiters-1.0.0.jar
ENV JWT_SECRET=${JWT_SECRET} \
DB_USER_NAME=${DB_USER_NAME} \
DB_PASSWORD=${DB_PASSWORD} \
DB_URL=${DB_URL} \
KAKAO_CLIENT_ID=${KAKAO_CLIENT_ID} \
KAKAO_URI=${KAKAO_URI} \
GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID} \
GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET} \
GOOGLE_URI=${GOOGLE_URI} \
NAVER_CLIENT_ID=${NAVER_CLIENT_ID} \
NAVER_CLIENT_SECRET=${NAVER_CLIENT_SECRET} \
NAVER_URI=${NAVER_URI} \
REDIS_HOST=${REDIS_HOST} \
REDIS_PORT=${REDIS_PORT}
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]