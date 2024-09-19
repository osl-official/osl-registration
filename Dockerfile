FROM openjdk:18-jdk-slim

WORKDIR /app

# Copy the contents of the local 'target' directory to the '/app' directory in the container
COPY ./target /app

# Set the environment variable for the Discord token
ENV token=${token}
ENV version=${version}

# Specify the command to run your Java application
ENTRYPOINT java -jar /app/Osl-Registration-${version}-jar-with-dependencies.jar $token