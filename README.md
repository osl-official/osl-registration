# OSL Registration Bot
A discord bot to handle team and free agent registration in Oceanic Slapshot League coded in JDA using Maven.


## How to Run
1. Set up the required variables in src/main/resources/settings.yml
   ```yaml
   ---
   token: "discord-bot-token"
   
   fa-registration: 9999999999999999
   team-registration: 8888888888888
   roster-channel: 777777777777777
   ```
2. Package the Jar using the following in a bash terminal.
   ```shell
   mvn clean package
   ```
   
### Using CLI
3. Run the jar using the following command once it has finished packaging.
    
   *Note: Set the version to be the same as the version defined in POM.xml or found in the target directory*
   ```shell
   java -jar target/Osl-Registration-{version}.jar "token"
   ```

### Using Docker
3. Run the following.
   ```shell
   docker compose up
   ```
   To run the app in the background add the `-d` flag.
   ```shell
   docker compose up -d
   ```

## Features
TODO

## TODO List
- [ ] Unit Test
- [ ] Add ability to stop registration at certain date
- [ ] Add ability to start registration at certain date
- [ ] Add ability to set season number
