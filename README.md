# [YEPPBot - YEPP it's a Twitch-Bot](https://www.Twitch.tv/YEPPBotV2)

## Table of Contents
- [Description](#description)
- [Usage](#usage)
- [Troubleshooting](#troubleshooting)
- [Host your own instance](#host-your-own-instance)
  - [Docker](#docker)
  - [Precompiled JAR](#precompiled-jar)
  - [Manual Setup](#manual-setup)
  - [Configuration](#configuration)
    - [Bot Configuration](#bot-configuration)
    - [Channel List](#channel-list-optional)
    - [MariaDB Database](#mariadb-database)
    - [HTTPS Server](#https-server)
    - [API Keys](#api-keys)
    - [OpenAI API](#openai-api)


<br> <br>


## Description
The YEPPBot is a Twitch-Bot that provides a variety of features to entertain and manage your Twitch channel. <br>

The original idea comes from [FoxxHimself](https://GitHub.com/lennartfu) and was written in Python back in the first half of 2021. <br>
After the original creator stopped working on and maintaining the bot, I took over its maintenance and decided to rewrite it in Java. <br>
The bot has since been fully rewritten in Java 21 using the [Twitch4J](https://Twitch4J.GitHub.io/) library. <br> <br>


## Usage
You can try out the bot on [Twitch](https://www.Twitch.tv/YEPPBotV2/chat) to see its features in action. <br>
You can add the bot to your channel by using the `!mod join` command. <br>
If you ever want to remove the bot from your channel, use the `!mod leave` command in your channel. <br>

For all features to work as intended, you need to authenticate the bot with your Twitch account using the `!mod auth` command in your channel. <br>
While it's recommended to assign the bot a moderator role in your channel to unlock all features, this is not strictly necessary. <br> <br>


## Troubleshooting and Contribution
If you need help or have any questions, feel free to reach out via [Discord](https://www.mcmodersd.de/dc), [mail](mailto:business@mcmodersd.de) or [Twitch](https://www.Twitch.tv/mcmodersd). <br>
I typically respond within 24 hours, often much faster. <br>

If you're interested in collaborating or need a customized version to suit your needs? <br>
Simply email [business@mcmodersd.de](mailto:business@mcmodersd.de). <br>

The bot is currently under active development, and some features may undergo significant changes in the future. <br>
So please note that the setup process for the bot may also change over time. <br> <br>


## Host your own instance
You can use this project as a template to create your own bot or to host your own instance of it. <br>
There are three ways to run the bot:
- [Docker](#docker)
- [Precompiled JAR](#precompiled-jar)
- [Manual Setup](#manual-setup)

But for all three methods, you need to create the configuration files yourself. <br>
Just follow the [Configuration](#configuration) section to create the configuration files. <br>
If you want a template for the configuration files, you can use the `-generate` argument to generate example files. <br> <br>

<hr>

### Docker
You can run the bot in a Docker container. <br>
You can find the Docker image on [Docker Hub](https://hub.docker.com/r/mcmodersd/yeppbot). <br>
Simply put all configuration files in a directory and mount it to the container at `/app/config`. <br> <br>

<hr>

### Precompiled JAR
First of all, you need to have Java 21 installed on your computer. <br>
You can download it from [here](https://www.oracle.com/uk/java/technologies/downloads/#java21). <br>
You can download the latest version of the bot from the [releases](https://github.com/MCmoderSD/YEPPBot/releases/latest) page. <br>
You can run the bot using the precompiled JAR file. <br>
You have to specify the absolute path to the configuration files when starting the bot. <br>
```bash
java -jar YEPPBot.jar -bot "/path/to/bot.json" -sql "/path/to/sql.json" -server "/path/to/server.json" and so on...
```
The run arguments for configuration files are:
- `-bot "/path/to/bot.json"`: Specifies the path to the bot configuration file. (required)
- `-channels "/path/to/channels.txt"`: Specifies the path to the channel list file. (optional)
- `-sql "/path/to/sql.json"`: Specifies the path to the MariaDB configuration file. (required)
- `-server "/path/to/server.json"`: Specifies the path to the HTTPS server configuration file. (required)
- `-api "/path/to/api.json"`: Specifies the path to the API keys configuration file. (optional)
- `-openai "/path/to/openai.json"`: Specifies the path to the ChatGPT configuration file. (optional)

There are also some other arguments you can use:
- `-help`: Displays a list of all available arguments.
- `-version`: Outputs the current version of the bot.
- `-cli`: Runs the bot in command-line interface mode.
- `-nolog`: Disables database logging.
- `-noninteractive`: Disables interactive mode.
- `-container`: Activates the Docker container mode.
- `-generate`: Generates example configuration files.

<br> <hr>

### Manual Setup
You can also compile the bot yourself and run it manually. <br>
You need to have Java 21 installed on your computer. <br>
You can download it from [here](https://www.oracle.com/uk/java/technologies/downloads/#java21). <br>
Then simply clone the project into your IDE and put the configuration files in the `/src/main/resources/` directory. <br>
- Put the `bot.json`, `channels.txt`, and `server.json` files in the `/src/main/resources/config/` directory. <br>
- Put the `sql.json` file in the `/src/main/resources/database/` directory. <br>
- Put the `api.json` and `openai.json` files in the `/src/main/resources/api/` directory. <br>

Then just build and run the project. <br> <br>


## Configuration
But before you can start configuring the bot, you need to do a few things. <br>
1. Create a Twitch application in the [Twitch Developer Console](https://dev.twitch.tv/console/apps).
2. Create a Twitch Account for the bot. (You can also use your own account, but it's not recommended).
3. Create a MariaDB database and user for the bot.

Keep in mind that the account token can become invalid after a while and if you change the password of the account. <br>
But remember **DON'T POST** or ever **SHARE** the token anywhere!!! <br> <br>


### Bot Configuration
The first file is `bot.json` and should have the following structure: <br>
```json
{
  "botId": "YOUR_BOT_ID",
  "botName": "YOUR_BOT_NAME, YOUR_BOT_ALIAS, YOUR_BOT_ALIAS",
  "botToken": "YOUR_BOT_TOKEN",
  "clientId": "YOUR_CLIENT_ID",
  "clientSecret": "YOUR_CLIENT_SECRET",
  "prefix": "! ¡",
  "admins": "ADMIN_NAME; OTHER_ADMIN_NAME"
}
```
- **botId**: The Twitch user ID of the bot account, which you can obtain using this [username to id converter](https://www.streamweasels.com/tools/convert-twitch-username-to-user-id/).
- **botName**: The name of the bot account. You can use multiple names separated by commas so the bot can recognize when it's mentioned.
- **botToken**: The token of the bot account, which you can generate [here](https://twitchapps.com/tmi/).
- **clientId** and **clientSecret**: The credentials for your Twitch application.
  - You need to create an application in the [Twitch Developer Console](https://dev.twitch.tv/console/apps).
  - Set `https://localhost:PORT/callback` as the OAuth redirect URL or use a domain that points to your server.
  - Set the application category to **Chatbot** and the Client Type to **Confidential**.
- **prefix**: The character(s) the bot will use to recognize commands.
- **admins**: The users who will have access to admin commands. <br>
The prefix is the character that the bot will use to recognize commands. <br>
The admins are the users that have access to the admin commands. <br> <br>


### Channel List (optional)
The channel list is optional and is called `channels.txt`. It should have the following structure: <br>
```
CHANNEL_NAME
OTHER_CHANNEL_NAME
``` 
You can add as many channels as you like.
If no channel list is provided, then the bot will only be active in its own channel. <br> <br>


### MariaDB Database
You have to provide a MariaDB database for the bot to store the data. <br>
The bot will automatically create the necessary tables and handle the setup, but you must provide a configuration file. <br>

Create a MariaDB database and user for the bot. <br>
```sql
CREATE DATABASE DATABASE_NAME;
CREATE USER 'USER_NAME'@'%' IDENTIFIED BY 'USER_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE_NAME.* TO 'USER_NAME'@'%';
FLUSH PRIVILEGES;
```
If the bot is running on the same server as the database, you can use `localhost` instead of `%`. <br> <br>
Create a file named `sql.json` with the following structure: <br>
```json
{
  "host": "localhost",
  "port": "3306",
  "database": "DATABASE_NAME",
  "username": "USER_NAME",
  "password": "USER_PASSWORD"
}
```
- **host**: The hostname or IP address of your MariaDB server (e.g., `localhost`).
- **port**: The port your MariaDB server is listening on (default is `3306`).
- **database**: The name of the database the bot will use.
- **username**: The MariaDB user with access to the database.
- **password**: The password for the MariaDB user. <br>
Ensure that the specified user has full permissions over the entire database. <br>
If you don't want to use the database logging, you can use the `-nolog` argument. <br> <br>


### HTTPS Server
The bot includes a built-in HTTPS server for Twitch Helix API authentication and features that are planned to be added in the future. <br>

You need to create a file named `server.json` with the following structure: <br>
```json
{
  "hostname": "YOUR_HOSTNAME",
  "port": 443,
  "host": true,
  "proxy": "YOUR_PROXY",

  "SSL": {
    "fullchain": "/path/to/fullchain.pem",
    "privkey": "/path/to/privkey.pem"
  },

  "JKS": {
    "file": "keystore.jks",
    "password": "secure_password",
    "validity": 365,
    "CN": "Your Name",
    "OU": "Your Organization Unit",
    "O": "Your Organization",
    "L": "Your Location",
    "ST": "Your State",
    "C": "Your Country"
  }
}
```
#### Fields Explained:
- `hostname`: Specify the hostname or IP address the server will bind to (e.g., `localhost` or a public domain).
- `port`: The port number for the server (default: `443`).
- `SSL`: If you're using the docker image, make sure to also mount the directory containing the certificate files.
  - `fullchain`: Path to the SSL/TLS certificate chain file (`.pem` format).
  - `privkey`: Path to the corresponding private key file.
- `JKS` (Java KeyStore): Optional configuration to use a JKS file for SSL instead of `.pem` files.
  - `file`: Path to the Java KeyStore file.
  - `password`: Password to access the JKS.
  - `validity`: Number of days the generated certificate is valid (used when creating a new keystore).
  - `CN`: Common Name (e.g., your domain name).
  - `OU`, `O`, `L`, `ST`, `C`: Organizational and geographical details for the certificate.

If you have a domain, you must use a valid certificate. Self-signed certificates are only allowed for `localhost`.
You can obtain a free certificate from [Let's Encrypt](https://letsencrypt.org/). <br> <br>


### API Keys
To configure the bot with API keys, create a file named `api.json` in the `/src/main/resources/api/` folder. <br>
The file should follow this structure:
```json
{
  "astrology": {
    "clientId": "YOUR_ASTROLOGY_CLIENT_ID, OPTIONAL_SECOND_CLIENT_ID",
    "clientSecret": "YOUR_ASTROLOGY_CLIENT_SECRET, OPTIONAL_SECOND_CLIENT_SECRET"
  },

  "giphy": "YOUR_GIPHY_API_KEY",
  "openWeatherMap": "YOUR_OPEN_WEATHER_MAP_API_KEY",
  "riot": "YOUR_RIOT_API_KEY"
}
```
If you do not wish to use a specific API or do not have the corresponding API key, you can omit that part of the configuration.
#### API Key Sources:
- **Astrology API**: Currently only used for the horoscope feature. Obtain your key from [Prokerala](https://api.prokerala.com/).
- **OpenWeatherMap API**: Used for the weather command. Obtain your key from [OpenWeatherMap](https://openweathermap.org/api).
- **Giphy API**: Used for GIF integration. Obtain your key from [Giphy](https://developers.giphy.com/). <br> <br>
- **Riot API**: Used for the League of Legends command. Obtain your key from [Riot Games](https://developer.riotgames.com/). <br> <br>


### OpenAI API
You only need to configure this module if you plan to use it. <br>
For example, if you don’t want to use the image module, you can omit the image part of the configuration. <br>

To set up, create a file named `openai.json` in the `/src/main/resources/api/` folder. <br>
The file should have the following structure: <br>
```json
{
  "apiKey": "YOUR_API_KEY",
  "user": "YOUR_USERNAME",
  "organizationId": "YOUR_ORGANIZATION_ID",
  "projectId": "YOUR_PROJECT_ID",

  "chat": {
    "model": "gpt-4o-mini-2024-07-18",
    "temperature": 1,
    "topP": 1,
    "frequencyPenalty": 0,
    "presencePenalty": 0,
    "n" : 1,
    "maxTokens": 120,
    "instruction": "You are the best TwitchBot that ever existed!",
    "spendingLimit": 32768,
    "priceFactor": 0.25
  }
}
```
Note: <br>
- Obtain your API key from [OpenAI](https://platform.openai.com/signup). <br>
- The `user` field such as the `organizationId` and `projectId` are optional. Leaving them empty or removing them will not affect the functionality of the wrapper. <br>
- The IDs are used for tracking purposes and can be obtained from the [OpenAI dashboard](https://platform.openai.com/settings/organization/general). <br>

<hr>

### Chat Configuration

| **Field**        | **Description**                                                                                |
|:-----------------|:-----------------------------------------------------------------------------------------------|
| model            | Model used for generating text. (Default: `chatgpt-4o-latest`)                                 |
| temperature      | Controls randomness: `0` (deterministic) to `2` (creative). (Default: `1`)                     |
| maxOutputTokens  | Maximum tokens in a response. So 500 characters are approximately 125 tokens). (Default `120`) |
| topP             | Nucleus sampling: `0` (plain) to `1` (creative). (Default: `1`)                                |
| frequencyPenalty | Reduces repetition of words. Values range from `0` to `2`. (Default: `0`)                      |
| presencePenalty  | Discourages repeating words from the conversation. Values range from `0` to `2` (Default: `0`) |
| devMessage       | Provides guidance for the bot's behavior.                                                      |
| spendingLimit    | Effective token spending limit, before chat gets resettet (Recommended: `32768`)               |
| priceFactor      | Price factor between input and output token price (Depends on the model)                       |




















However, it is necessary to create a [Twitch application](https://dev.twitch.tv/console) and provide access to a MariaDB database. <br>
For ChatGPT features, as well as certain other features, you need to provide their respective API keys for them to function properly. <br>

If you have any ideas or suggestions, feel free to open an issue or submit a pull request. <br> <br>


### 1. Download the bot jar file
You can download the latest version of the bot from the [releases](https://github.com/MCmoderSD/YEPPBot/releases/latest) page. <br>
Create the configuration files yourself, or use the `-generate` argument to create example files. <br>

YAlternatively, you can clone the repository and compile the bot yourself.
To do this, ensure you have Git installed on your computer. You can download it from [here](https://git-scm.com/downloads). <br>

Clone the repository using the following command: <br>
```bash
git clone https://www.GitHub.com/MCmoderSD/YEPPBot.git 
``` 
<br>


### 7. Running the bot
After compiling the bot into a `.jar` file, you can run it using the following command:
```bash
java -jar JAR_FILE.jar
```

If you are using the precompiled `.jar` file to generate example configuration files, use:
```bash
java -jar YEPPBot.jar -generate
```

Once you have edited the configuration files and added the necessary API keys, you can start the bot with:
```bash
java -jar YEPPBot.jar -botconfig "/path/to/bot.json" -sql "/path/to/sql.json" -httpsserver "/path/to/server.json"
```
You can include additional arguments for other configuration files as needed.

<hr>

#### General Arguments:
- `-help`: Displays a list of all available arguments. 
- `-version`: Outputs the current version of the bot. 
- `-cli`: Runs the bot in command-line interface mode. 
- `-nolog`: Disables database logging.
- `-noninteractive`: Disables interactive mode.
- `-generate`: Generates example configuration files.


#### Configuration File Arguments:
- `-botconfig "/path/to/bot.json"`: Specifies the path to the `bot.json` file. 
- `-channellist "/path/to/channels.txt"`: Specifies the path to the `channels.txt` file. 
- `-sql "/path/to/sql.json"`: Specifies the path to the `sql.json` file. 
- `-server "/path/to/server.json"`: Specifies the path to the `server.json` file. 
- `-api "/path/to/api.json"`: Specifies the path to the `api.json` file.
- `-openai "/path/to/openai.json"`: Specifies the path to the `openai.json` file. <br> <br>


## Usage and commands
The bot offers a variety of commands to enhance functionality and interactivity. <br>
Below is an overview of some key commands: <br>

#### General Commands:
- `!help commands`: Displays a list of all available commands.
- `!help COMMAND_NAME`: Provides detailed information about a specific command.

#### Moderation Commands:
- `!moderate join`: Makes the bot join your channel.
- `!moderate leave`: Makes the bot leave your channel.
- `!moderate block COMMAND_NAME`: Blacklists a command, preventing it from being used.
- `!moderate unblock COMMAND_NAME`: Removes a command from the blacklist.

#### Counter Commands:
- `!counter`: Create and manage counter-commands for various purposes.

#### Custom Commands:
The bot supports creating and managing custom commands with the !CustomCommand command. <br>
Custom commands can include dynamic variables to make them more interactive:
- `%author%`: Replaced with the username of the person executing the command. 
- `%channel%`: Replaced with the name of the channel where the command is executed. 
- `%tagged%`: Replaced with the first word following the command (useful for mentions). 
- `%random%`: Replaced with a random percentage between 0 and 100. <br> <br>


## Docker Setup
Since YEPPBot v1.23.0, you are able to run the bot in a Docker container. <br>
To do this, you need to have Docker installed on your computer. You can download it from [here](https://www.docker.com/products/docker-desktop). <br>

The setup process is similar to the manual setup, but makes managing the bot easier. <br>
You can use the `mcmodersd/yeppbot` image from [Docker Hub](https://hub.docker.com/r/mcmodersd/yeppbot). <br>

Following files are required:
- The Bot Configuration named `bot.json`.
- The MariaDB Configuration named `sql.json`.
- The HTTPS Server Configuration named `server.json`.

The Channel List, API Keys, and ChatGPT Configuration are optional.
- The Channel List is named `channels.txt`.
- The API Keys are named `api.json`.
- The ChatGPT Configuration is named `openai.json`.

### 2. Running the Docker Container
To run the bot in a Docker container, use the following command:
```bash
docker run -d --name YEPPBot -p <port>:443 -v /path/to/config:/app/config mcmodersd/yeppbot
```
You need to replace `<port>` with the port you want to use for the HTTPS server. (default 443) <br>
You need to replace `/path/to/config` with the path to the directory containing the configuration files. <br>

If you're using the SSL Certificate instead of the JKS files, you need to mount the directory containing the certificate files. <br>

## YEPPConnect
**YEPPConnect** is a feature that enables your viewers to whitelist their Minecraft usernames directly through the bot.

### Whitelist Commands:
- `!whitelist add McName`: Adds the specified Minecraft username (`McName`) to the whitelist.
- `!whitelist remove McName`: Removes the specified Minecraft username (`McName`) from the whitelist.

### Minecraft Server Setup:
To integrate YEPPConnect with your Minecraft server, follow these steps:

#### 1. Install the [YEPPConnect](https://github.com/MCmoderSD/YEPPConnect):
- This plugin connects your Minecraft server to the bot, allowing whitelist management directly from chat.
- It also lets you check the online status of the bot.

#### 2. Compatibility:
- The plugin supports Minecraft version 1.13 and above. 
- Make sure you are using YEPPConnect v1.21.0 or later for full functionality.

#### 3. Documentation:
- Refer to the [plugin documentation](https://github.com/MCmoderSD/YEPPConnect?tab=readme-ov-file#commands-and-permissions) for installation instructions, commands, and permissions. <br> <br>


## Contributing
Have an idea or suggestion? Feel free to:
- Open an issue or pull request on GitHub.
- Reach out to me on [Discord](https://www.mcmodersd.de/dc) or [Twitch](https://www.twitch.tv/mcmodersd).

### Acknowledgments
Special thanks to:
- [FoxxHimself](https://github.com/lennartfu): For creating the original bot and inspiring its rewrite in Java. 
- [RedSmileTV](https://github.com/redsmiletv): For assistance with the bot, APIs, and libraries. 
- [Rebix](https://github.com/reebix): For support with the bot and related integrations.
- [r4kunnn](https://www.twitch.tv/r4kunnn): For testing and feedback on the bot's features.

Your contributions and support are greatly appreciated. Together, we can make the bot even better! <br> <br>




## Features

### Commands for everyone:
- Conversation
- Fact
- Gift
- Help
- Horoscope
- Insult
- Joke
- Lurk
- Match
- Prompt
- Translate
- Weather
- Wiki

### Commands for Broadcaster and Moderators:
- Counter
- CustomCommand
- CustomTimer
- Moderate
- Quote
- Rank
- Say
- Social
- Shoutout

### Admin/Debug Commands:
- Info
- Ping
- Status

### Command Features:
- Birthday
  - The bot will congratulate the user on his birthday.
  - You can set the birthday of the user with the `!birthday set` command.
  - You can list all the birthdays with the `!birthday list` command.
  - You can look up the next birthday with the `!birthday next` command.

- Seasonal Event Commands
  - DickDestroyDecember
  - NoNutNovember

- Whitelist
  - The bot can whitelist users for a Minecraft server.
  - Viewers can whitelist themselves using the `!whitelist add` command.

### Planned Features:
- [ ] Web UI
- [ ] Key Command
- [ ] Discord Integration