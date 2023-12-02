# OSL Registration Bot
A discord bot to handle team and free agent registration in Oceanic Slapshot League coded in JDA using Maven.

## Prerequisites
A list of general pre-requisites to run the discord bot. A basic knowledge of each is assumed.
- Docker
- Make
- Java JDK 18+
- Maven

## How to Run
1. Set up the required variables in src/main/resources/settings.yml
   ```yaml
   ---   
   fa-registration: 9999999999999999
   team-registration: 8888888888888
   roster-channel: 777777777777777
   ```

2. Run the appropriate Make command. The token variable is required and should be set to the discord token.
   
   *Note: Set the version to be the same as the version defined in POM.xml or found in the target directory*
   1. ### Locally
      ```shell
      make deploy_app_locally token="..." version="2.0.0"
      ```

   2. ### Docker
      ```shell
      make deploy_app_in_docker token="..." version="2.0.0"
      ```

## Features
TODO

## TODO List
- Unit Test
- Add ability to stop registration at certain date
- Add ability to start registration at certain date
- Add ability to set season number
- Improve documentation