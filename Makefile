token ?= ""
version ?= "1.0-SNAPSHOT"

docker_stop:
	-docker stop osl-registration-container
	-docker rm osl-registration-container
	-docker rmi osl-registration-image

deploy_app_in_docker:  # make deploy_app_in_docker token="..." version="2.0.0"   Version is optional
	@if [ -z "$(token)" ]; then \
  		echo "Error: Discord token needs to be provided"; \
  		exit 1; \
	fi
	mvn package
	make docker_stop
	docker build -t osl-registration-image .
	docker run --name osl-registration-container -e token=${token} -e version=${version} osl-registration-image

deploy_app_locally: # make deploy_app_locally token="..." version="2.0.0"   Version is optional
	@if [ -z "$(token)" ]; then \
  		echo "Error: Discord token needs to be provided"; \
  		exit 1; \
	fi
	mvn package
	java -jar target/Osl-Registration-${version}.jar ${token}