# YEPPBot - YEPP it's a Twitch Bot

## Description

The YEPPBot is a Twitch bot that provides a variety of features to entertain your Twitch channel. <br>
Originally created by [FoxxHimself](https://github.com/lennartfu) in Python and now being rewritten
in [Java 17](https://www.oracle.com/de/java/technologies/downloads/#java17) using
thr [Twitch4J library](https://twitch4j.github.io/).

The bot is currently under active development and can change a lot in the future. <br>
If you have any ideas or suggestions, feel free to open an issue or a pull request. <br> <br>

## Table of Contents
- [Description](#description)
- [Features](#features)
- [How to use](#how-to-use)
- [Usage and commands](#usage-and-commands)
- [Contributing](#contributing)

<br>

## Features

- [x] Fact command
- [x] Insult command
- [x] Lurk command with timer
- [x] Joke command
- [x] Weather command
- [x] Wiki command
- [x] Admin commands
- [x] Debug Commands
- [x] Database logging
- [x] Help command
- [x] Gif command
- [x] Key
- [x] ChatGPT command
- [x] Translate command
- [x] Join and Leave chat command
- [x] Black and White list
- [x] Graphical User Interface
- [ ] Rank command

<br>

## How to use

You need to have Java 17 installed in your computer, you can download it
from [here](https://www.oracle.com/uk/java/technologies/downloads/#java17). <br>
You need the Twitch Account Token of the Twitch Bot, you can get it from [here](https://twitchapps.com/tmi/).<br> <br>
Keep in mind that the token can change from to time. <br>
But remember **don't EVER POST or SHARE the token anywhere**!!! <br> <br>

### 1. Clone the repository

You need to have Git installed in your computer, you can download it from [here](https://git-scm.com/downloads). <br>
Clone the repository using the following command: <br>
```git clone https://www.github.com/MCmoderSD/YEPPBot.git ``` <br> <br>

### 2. Edit the bot config file

You need to create two JSON files in ```/src/main/resources/config/``` folder. <br> <br>
The first file is ```BotConfig.json``` and it should have the following structure: <br>

```json
{
  "botName": "YOUR_BOT_NAME",
  "botToken": "YOUR_BOT_TOKEN",
  "prefix": "!", 
  "admins": "ADMIN_NAME"
}
```

The prefix is the character that the bot will use to recognize commands. <br> <br>
The second file is ```Channel.list``` and it should have the following structure: <br>

```
CHANNEL_NAME
OTHER_CHANNEL_NAME
```

You can add as many channels as you want. <br> <br>

### 3. Add a ChatGPT API key

You need to create a file called ```ChatGPT.json``` in the ```/src/main/resources/api/``` folder. <br>
The file should have the following structure: <br>

```json
{
  "apiKey": "YOUR_API_KEY",
  "model": "gpt-3.5-turbo",
  "maxTokens": 120,
  "temperature": 1,
  "instruction": "You are the best TwitchBot that ever existed!"
}
```

You can get the API key from [OpenAI](https://platform.openai.com/signup). <br>
The model is the model that the bot will use to generate the text. <br>
You can set the max tokens as high as you want. <br>
The temperature is the randomness of the text. <br>
The lowest is 0 and the highest is 2. The higher the value, the more random the text will be. Lower values will make the text more subtile. <br>
The instruction is the way the bot should behave and how he should reply to the prompt. <br> <br>

### 5. Add an OpenWeatherMap API key

You need to create a file called ```OpenWeatherMap.json``` in the ```/src/main/resources/api/``` folder. <br>
The file should have the following structure: <br>

```json
{
  "url": "https://api.openweathermap.org/data/2.5/weather?q=",
  "api_key": "YOUR_API_KEY"
}
```
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

### 6. MySQL Database for logging

You need to have your own MySQL database to log the chat messages or contact me and can give you access to mine. <br>
You need to create a file called ```mySQL.json``` in the ```/src/main/resources/database/``` folder. <br>
The file should have the following structure: <br>

```json
{
  "host": "localhost",
  "port": "3306",
  "database": "DATABASE_NAME",
  "username": "USERNAME",
  "password": "PASSWORD"
}
```
<br>

### 7. Black and White list commands

You can edit the ```blacklist.json``` and ```whitelist.json```  files in the ```/src/main/resources/config/``` folder. <br>

It should have the following structure: <br>

```json
{
  "COMMAND_NAME": "Channel1; Channel2; Channel3",
  "OTHER_COMMAND_NAME": "Channel1; Channel2; Channel3"
}
```

You can add as many channels or commands as you want. <br>
The way for black and whitelist works the same. <br> <br>

### 8. Compile the bot
After you compiled the bot into a .jar file, you can run it using the following command: <br>
```java -jar NAME_OF_THE_JAR_FILE.jar``` <br> <br>

### 9. Edit source code (optional)

Before you compile the bot, you can edit the source code to add your own commands or features. <br>
You can disable the registration of the commands in the ```BotClient``` class in ```/src/main/java/de/MCmoderSD/core/BotClient.java``` <br>
You can simply delete the line of the command you want to disable. <br>
The commands are registered in the ```initCommands()``` method. <br> <br>

## Usage and commands

The bot has a variety of commands that you can use. <br>
You can use the ```!help``` command to get a list of all the commands. <br>
You can use the ```!help COMMAND_NAME``` to get more information about a specific command. <br>
You can use the ```!joinchat CHANNEL_NAME``` to make the bot join a specific channel. <br>
You can use the ```!leavechat CHANNEL_NAME``` to make the bot leave a specific channel. <br>

You can start the bot without the graphical user interface mode by running the bot with the ```-nogui``` argument. <br> <br>

## Contributing
If you have any ideas or suggestions, feel free to open an issue or a pull request. <br>
You can also contact me on [Discord](https://www.mcmodersd.de/dc) or [Twitch](https://www.twitch.tv/mcmodersd). 

Lot of thanks to [Twitch4J](https://twitch4j.github.io/) for the amazing library and to [OpenAI](https://platform.openai.com/signup) for the ChatGPT API. <br>
Also thanks to [OpenWeatherMap](https://openweathermap.org/api) for the weather API. <br>

Especially thanks to [FoxxHimself](https://github.com/lennartfu) for the original bot and the idea to rewrite it in Java. <br>
Lot of thanks to [RedSmileTV](https://github.com/redsmiletv) and [Rebix](https://github.com/reebix) for the help with the bot and api's and libraries. <br>