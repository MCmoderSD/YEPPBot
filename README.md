# YEPPBot - YEPP it's a Twitch Bot

## Description

The YEPPBot is a Twitch bot that provides a variety of features to entertain your Twitch channel. <br>
Originally created by [FoxxHimself](https://github.com/lennartfu) in Python and now compleatly rewritten in [Java 21](https://www.oracle.com/de/java/technologies/downloads/#java21) using the [Twitch4J](https://twitch4j.github.io/) library.

The bot is currently under active development and it's features can change a lot in the future. <br>
You can use the project to create your own bot, but you have to add a MySQL database and for certain features you need a config with an API key. <br>
If you don't have access to the API, the bot will still work, but without those features, but the database is necessary. <br>
If you have any ideas or suggestions, feel free to open an issue or a pull request. <br> <br>

## Table of Contents

- [Description](#description)
- [Features](#features)
- [How to use](#how-to-use)
- [Usage and commands](#usage-and-commands)
- [Contributing](#contributing)

<br>

## Features

- Commands:
  - [x] ChatGPT
  - [x] Fact
  - [x] Gif
  - [x] Insult
  - [x] Joke
  - [x] Lurk (with timer)
  - [x] Translate
  - [x] Weather
  - [x] Wiki


- Admin Commands:
  - [x] Block/Unblock
  - [x] Help
  - [x] Join/Leave
  - [x] Ping
  - [x] Say
  - [x] Status
  

- Features:
  - [x] Custom Commands
  - [x] Custom Counters
  - [x] Custom Timers (Still in development)
  - [x] Database Logging
  - [x] User Intercae


- Planned Features:
  - [ ] Web UI
  - [ ] Rank
  - [ ] Key
  

## How to use

You need to have Java 21 installed on your computer a download link can be found [here](https://www.oracle.com/uk/java/technologies/downloads/#java21). <br>
You need the Twitch Account Token of the Twitch Bot which you can get from [here](https://twitchapps.com/tmi/).<br> <br>
Keep in mind that the token can change from to time. <br>
But remember **DON'T POST** or **SHARE** the token anywhere!!! <br> <br>

### 1. Download the bot jar file

You can download the latest version of the bot from the [releases](https://github.com/MCmoderSD/YEPPBot/releases/latest) page. <br>
You need to create the config files and add the API keys, but if you don't want to compile the bot yourself, you can ignore the folder structure. <br>

You can also clone the repository and compile the bot yourself.
For that need to have Git installed on your computer, you can download it from [here](https://git-scm.com/downloads). <br>
Clone the repository using the following command: <br>
```git clone https://www.github.com/MCmoderSD/YEPPBot.git ``` <br> <br>

### 2. Edit the config files

You currently need to create two JSON files in ```/src/main/resources/config/``` folder. <br> <br>
The first file is ```BotConfig.json``` and it should have the following structure: <br>

```json
{
  "botName": "YOUR_BOT_NAME",
  "botToken": "YOUR_BOT_TOKEN",
  "prefix": "!",
  "admins": "ADMIN_NAME; OTHER_ADMIN_NAME"
}
```

The prefix is the character that the bot will use to recognize commands. <br> <br>
The second file is ```Channel.list``` and it should have the following structure: <br>

```
CHANNEL_NAME
OTHER_CHANNEL_NAME
```

You can add as many channels as you want.
<br> <br>

### 3. Add a ChatGPT API key

You need to create a file called ```ChatGPT.json``` in the ```/src/main/resources/api/``` folder. <br>
The file should have the following structure: <br>

```json
{
  "apiKey": "YOUR_API_KEY",
  "model": "gpt-4o-mini",
  "maxTokens": 100,
  "temperature": 1,
  "instruction": "You are the best TwitchBot that ever existed!"
}
```

You can get the API key from [OpenAI](https://platform.openai.com/signup). <br>
The model is the model that the bot will use to generate the text. <br>
You can set the max tokens as high as you want. <br>
The temperature is the randomness of the text. <br>
The lowest is 0 and the highest is 2. The higher the value, the more random the text will be. Lower values will make the
text weirder. <br>
The instruction is the way the bot should behave and how he should reply to the prompt. <br> <br>

### 4. Add an OpenWeatherMap API key
You need to create a file called ```OpenWeatherMap.json``` in the ```/src/main/resources/api/``` folder. <br>
The file should have the following structure: <br>

```json
{
  "url": "https://api.openweathermap.org/data/2.5/weather?q=",
  "api_key": "YOUR_API_KEY"
}
```

You need a ChatGPT API key to use the weather command. <br>
You can get the API key from [OpenWeatherMap](https://openweathermap.org/api). <br> <br>

### 5. Add a Giphy API key
You need to create a file called ```Giphy.json``` in the ```/src/main/resources/api/``` folder. <br>
The file should have the following structure: <br>

```json
{
  "url": "https://api.giphy.com/v1/gifs/random?api_key=",
  "api_key": "YOUR_API_KEY",
  "query": "&tag="
}
```

You can get the API key from [Giphy](https://developers.giphy.com/). <br> <br>

#
### 6. MySQL Database

You need to have your own MySQL database to run and use the bot. <br>
The Bot creates the tables and everything by itself, but you have to create a config. <br>
You need to create a file called ```mySQL.json``` in the ```/src/main/resources/database/``` folder. <br>
The file should have the following structure: <br>

```json
{
  "host": "localhost",
  "port": "3306",
  "database": "DATABASE_NAME",
  "username": "USER_NAME",
  "password": "USER_PASSWORD"
}
```

You have to give the user full permission over the whole database. <br>
If you don't want to use a database, you have to remove certain features from the source code. <br> <br>

### 7. Add assets to the database

For the fact, insult and joke command you need to add the assets to the database. <br>
You can import the existing ones to the database from the .tsv files in the ```/storage/assets/``` folder. <br> <br>
You can also add your own assets to the database, just pay attention to the format. <br>

### 8. Compile the bot

After you compiled the bot into a .jar file, you can run it using the following command: <br>
```java -jar NAME_OF_THE_JAR_FILE.jar``` 

If you use the downloaded jar file, you can run it using the following command: <br>
```java -jar YEPPBot.jar -botconfig "/PATH/TO/BotConfig.json" -mysql "/PATH/TO/mySQL.json"``` <br> <br>

You don't need a channel list file, but it's recommended to use one. <br>
You can use the ```-help``` argument to get a list of all the arguments. <br>
You can use the ```-version``` argument to get the version of the bot. <br> <br>
You can use the ```-cli``` argument to run the bot in the command line interface mode. <br>
You can use the ```-nolog``` argument to disable the database logging. <br>

For the configs you can use: <br>
the ```-botconfig "/PATH/TO/BotConfig.json"``` argument to specify the path to the BotConfig.json file. <br>
the ```-channellist "/PATH/TO/Channel.list"``` argument to specify the path to the Channel.list file. <br>
the ```-mysqlconfig "/PATH/TO/mySQL.json"``` argument to specify the path to the mySQL.json file. <br>
the ```-openaiconfig "/PATH/TO/ChatGPT.json"``` argument to specify the path to the ChatGPT.json file. <br>
the ```-openweathermapconfig "/PATH/TO/OpenWeatherMap.json"``` argument to specify the path to the OpenWeatherMap.json file. <br>
the ```-giphyconfig "/PATH/TO/Giphy.json"``` argument to specify the path to the Giphy.json file. <br> <br>

<br> <br>

## Usage and commands

The bot has a variety of commands that you can use. <br>

You can use the ```!help``` command to get a list of all the commands. <br>
You can use the ```!help COMMAND_NAME``` to get more information about a specific command. <br>

You can use the ```!moderate join/leave CHANNEL_NAME``` to make the bot join or leave a specific channel. <br>
You can use the ```!moderate block/unblock``` to blacklist commands. <br>

You can use the ```!CustomCommand``` command to create and manage custom commands. <br>
You can also use variables in the custom commands. <br>
%author% - will be replaced with the username of the person who executes the command<br>
%channel% - will be replaced with the channel name where the command was executed<br>
%tagged% - will be replaced by the first word after the command<br>
%random% - will be replaced by a random percentage between 0 and 100<br>

You can use the ```!Counter``` command to create and manage counter commands. <br> <br>

## Contributing

If you have any ideas or suggestions, feel free to open an issue or a pull request. <br>
You can also contact me on [Discord](https://www.mcmodersd.de/dc) or [Twitch](https://www.twitch.tv/mcmodersd).

Lot of thanks to [Twitch4J](https://twitch4j.github.io/) for the amazing library and
to [OpenAI](https://platform.openai.com/signup) for the ChatGPT API. <br>
Also thanks to [OpenWeatherMap](https://openweathermap.org/api) for the weather API. <br>

Especially thanks to [FoxxHimself](https://github.com/lennartfu) for the original bot and the idea to rewrite it in Java. <br>
Lot of thanks to [RedSmileTV](https://github.com/redsmiletv) and [Rebix](https://github.com/reebix) for the help with
the bot and api's and libraries. <br>