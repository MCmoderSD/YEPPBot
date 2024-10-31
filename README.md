# YEPPBot - YEPP it's a Twitch Bot

## Description

The YEPPBot is a Twitch bot that provides a variety of features to entertain your Twitch channel. <br>
Originally created by [FoxxHimself](https://github.com/lennartfu) in Python and now compleatly rewritten in [Java 21](https://www.oracle.com/de/java/technologies/downloads/#java21) using the [Twitch4J](https://twitch4j.github.io/) library.

The bot is currently under active development and features can change a lot in the future. <br>
So keep in mind setting up the bot can change from time to time. <br> <br>

You can use the project to create your own bot, either by modifying the source code or by using the [jar](https://github.com/MCmoderSD/YEPPBot/releases/latest). <br>
The setup is a bit complicated and requires a lot of configuration and troubleshooting. <br>

You don't need all API keys to run the bot, just for certain features. <br>
What you definitely need is a Twitch Account and a MySQL Database. <br>
If you have any ideas or suggestions, feel free to open an issue or a pull request. <br> <br>

## Table of Contents

- [Description](#description)
- [Features](#features)
- [Installation](#installation-and-usage)
- [YEPPConnect](#yeppconnect)
- [Usage and commands](#usage-and-commands)
- [Contributing](#contributing)

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
- Say

### Admin/Debug Commands:

- Info
- Ping
- Status

### Command Features:

- Birthday
  - The bot will congratulate the user on his birthday.
  - You can set the birthday of the user with the ```!birthday set``` command.
  - You can list all the birthdays with the ```!birthday list``` command.
  - You can look up the next birthday with the ```!birthday next``` command.


- Seasoal Event Commands
  - DickDestroyDecember
  - NoNutNovember


- Whitelist
  - The bot can whitelist users for a Minecraft server.
  - Viewers can whitelist themselves using the ```!whitelist add``` command.
  

### Planned Features:

  - [ ] Web UI
  - [ ] Rank Command
  - [ ] Key Command
  - [ ] Discord Integration
  

## Installation and Usage

If you need help or have any questions, feel free to contact me on [Discord](https://www.mcmodersd.de/dc) or via [Mail](mailto:business@mcmodersd.de), you can also contact me on [Twitch](https://www.twitch.tv/mcmodersd). <br>
I respond within 24 hours, usually a lot faster. If you want to cooperate? need a version that fit your needs? <br> 
Just write a [Mail](mailto:business@mcmodersd.de) to [business@mcmodersd.de](mailto:business@mcmodersd.de) <br> <br>

You need to have Java 21 installed on your computer a download link can be found [here](https://www.oracle.com/uk/java/technologies/downloads/#java21). <br>
You need the Twitch account token of the Twitch bot which you can get from [here](https://twitchapps.com/tmi/).<br>
Then you need to register the bot on the [Twitch Developer Console](https://dev.twitch.tv/console/apps). <br>

Keep in mind that the account token can change from to time. <br>
But remember **DON'T POST** or **SHARE** the token anywhere!!! <br> <br>

### 1. Download the bot jar file

You can download the latest version of the bot from the [releases](https://github.com/MCmoderSD/YEPPBot/releases/latest) page. <br>
You can create the Config files yourself or use the ```-generate``` argument to create the config files. <br>
You can skip to [Step 8](#8-compile-the-bot) if you use the downloaded jar file. <br> <br>

You can also clone the repository and compile the bot yourself.
For that need to have git installed on your computer, you can download it from [here](https://git-scm.com/downloads). <br>
Clone the repository using the following command: <br>
```bash
git clone https://www.github.com/MCmoderSD/YEPPBot.git 
``` 

### 2. Edit the config files

You currently need to create two JSON files in ```/src/main/resources/config/``` folder. <br> <br>
The first file is ```BotConfig.json``` and it should have the following structure: <br>

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

The botId can be obtained with this tool [here](https://www.streamweasels.com/tools/convert-twitch-username-to-user-id/). <br>
The prefix is the character that the bot will use to recognize commands. <br> <br>
The second file is ```Channel.list``` and it should have the following structure: <br>

```
CHANNEL_NAME
OTHER_CHANNEL_NAME
```

You can add as many channels as you want.
<br> <br>

### 3. MySQL Database

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
If you don't want to use the database logging, you can use the ```-nolog``` argument to disable it. <br> <br>
You can use the ```-generate``` argument to generate an example config file. <br> <br>


### 4. HTTPS Server

The YEPPBot creates a simple HTTPS server to manage the Helix API authentication. <br>
It can also be used to broadcast sound over a browser page. <br>
You need to create a file called ```httpsServer.json``` in the ```/src/main/resources/config/``` folder. <br>
The file should have the following structure: <br>

```json
{
  "hostname": "YOUR_HOSTNAME",
  "port": 420,
  "keystore": "/keys/keystore.jks",
  "fullchain": "/path/to/fullchain.pem",
  "privkey": "/path/to/privkey.pem"
}
```

You can also use localhost with a self-signed jks file. <br>
Then the Twitch API will work, but OBS won't work with the browser source as it doesn't accept self-signed jks files certificate. <br>
You can use the ```-generate``` argument to generate an example config file. <br> <br>

You can generate a self-signed certificate using the following command: <br>
```bash
keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 360 -keysize 2048
```
As password, you have set the bot token, but as SHA-256 hash, encoded in base64.<br>
You can use this [SHA-256 hash tool](https://emn178.github.io/online-tools/sha256.html) to convert the bot token into an SHA-256 Base64 encoded hash. <br>
Just make sure to set **UTF-8** as input and **Base64** as output format. <br>

If you have a domain, you **NEED** to use a valid certificate, if you use a self-signed jks file you need to use localhost. <br>
You can get a free valid certificate from [Let's Encrypt](https://letsencrypt.org/). <br> <br>


### 5. Add API Keys
You need to create a file called ```apiKeys.json``` in the ```/src/main/resources/api/``` folder. <br>
The file should have the following structure: <br>

```json
{
  "astrology": {
    "clientId": "YOUR_ASTROLOGY_CLIENT_ID, OPTIONAL_SECOND_CLIENT_ID",
    "clientSecret": "YOUR_ASTROLOGY_CLIENT_SECRET, OPTIONAL_SECOND_CLIENT_SECRET"
  },

  "giphy": "YOUR_GIPHY_API_KEY",
  "openWeatherMap": "YOUR_OPEN_WEATHER_MAP_API_KEY"
}
```

You can use the ```-generate``` argument to generate an example config file. <br> 

If you don't want to use the API or just don't have the API key, you can just delete the part of the config. <br> <br>

The astrology API is currently not used, but it's planned to be used in the future. <br>
You can obtain the AstrologyAPI key from [Prokerala](https://api.prokerala.com/). <br>
You can obtain the OpenWeatherMap API key from [OpenWeatherMap](https://openweathermap.org/api). <br>
You can obtain the Giphy API key from [Giphy](https://developers.giphy.com/). <br> 

For the astrology and weather feature, you also need the ChatGPT API key. <br> <br>


### 6. Add a ChatGPT API key

You only need to configure the module if you want to use, if you for example doesn't want to use the image module, you can just delete the image part of the config. <br>
You need to create a file called ```ChatGPT.json``` in the ```/src/main/resources/api/``` folder. <br>
The file should have the following structure: <br>

```json
{
  "apiKey": "YOUR_API_KEY",

  "chat": {
    "chatModel": "gpt-4o-mini-2024-07-18",
    "maxConversationCalls": 10,
    "maxTokenSpendingLimit": 8192,
    "temperature": 1,
    "maxTokens": 120,
    "topP": 1,
    "frequencyPenalty": 0,
    "presencePenalty": 0,
    "instruction": "You are the best TwitchBot that ever existed!"
  },

  "image": {
    "imageModel": "dall-e-2",
    "quality": "standard",
    "resolution": "1024x1024",
    "style": "vivid"
  },

  "speech": {
    "ttsModel": "tts-1",
    "voice": "alloy",
    "speed": 1,
    "format": "wav"
  },

  "transcription": {
    "transcriptionModel": "whisper-1",
    "prompt": "Transcribe the following audio file to German.",
    "language": "German",
    "temperature": 1
  }
}
```

You can get the API key from [OpenAI](https://platform.openai.com/signup). <br>
You can use the ```-generate``` argument to generate an example config file. <br>
But if you don't want to use the ChatGPT module, you can just delete the chat part of the config. <br> <br>


### Chat Configuration
- The **chatModel** is the model that the bot will use to generate the text. <br>
  The available models are: <br>

| **Model**              | **Pricing**                                               | 
|:-----------------------|:----------------------------------------------------------|
| gpt-4o                 | $5.00 / 1M input tokens <br/> \$15.00 / 1M output tokens  |
| gpt-4o-2024-08-06      | $2.50 / 1M input tokens <br/> \$10.00 / 1M output tokens  |
| gpt-4o-2024-05-13      | $5.00 / 1M input tokens <br/> \$15.00 / 1M output tokens  |
| gpt-4o-mini            | $0.150 / 1M input tokens <br/> \$0.600 / 1M output tokens |
| gpt-4o-mini-2024-07-18 | $0.150 / 1M input tokens <br/> \$0.600 / 1M output tokens |


- The **maxConversationCalls** is the limit of calls per conversation. <br>
  After the limit is reached, the conversation will end. <br>


- The **maxTokenSpendingLimit** is the limit of tokens spent per conversatition. <br>
  After the limit is reached, the conversation will end. <br>


- The **temperature** is the randomness of the text. <br>
  Lowering results in less random completions. As the temperature approaches zero, the model will become deterministic
  and repetitive. <br>
  Higher temperature results in more random completions. <br>
  The min value is 0 and the max value is 2. <br>


- The **maxTokens** is the maximum length of the response text. <br>
  One token is roughly 4 characters for standard English text. <br>
  The limit is 16383 tokens, but it's recommended to use a value that is suitable for the use, on Twitch the message
  limit is 500 characters.
  If you divide the limit by 4, you an estimate the number of characters. <br>


- The **topP** is the nucleus sampling. <br>
  The lower the value, the more plain the text will be. <br>
  The higher the value, the more creative the text will be. <br>
  The min value is 0 and the max value is 1. <br>


- The **frequencyPenalty** reduces the likelihood of repeating the same words in a response.
  The higher the value, the less the bot will repeat itself. <br>
  The min value is 0 and the max value is 1. <br>


- The **presencePenalty** reduces the likelihood of mentioning words that have already appeared in the
  conversation. <br>
  The higher the value, the less the bot will repeat itself. <br>
  The min value is 0 and the max value is 1. <br>


- The **instruction** is the way the bot should behave and how he should reply to the prompt. <br>


### Image Configuration

- The **imageModel** is the model that the bot will use to generate the image. <br>
  The available models are: <br>

| **Model** | **Quality** | **Resolution**                     | **Pricing**                                             |
|:----------|:-----------:|:-----------------------------------|:--------------------------------------------------------|
| dall-e-2  |             | 256x256<br/>512x512<br/>1024x1024  | \$0.016 / Image<br/>\$0.018 / Image<br/> $0.020 / Image |
| dall-e-3  |  standard   | 1024x1024<br/>1024x1792, 1792×1024 | \$0.040 / Image<br/>\$0.080 / Image                     |
| dall-e-3  |     hd      | 1024x1024<br/>1024x1792, 1792×1024 | \$0.080 / Image<br/>\$0.120 / Image                     |


- The **quality** is the quality of the image. <br>
  The available qualities are standard and hd. <br>
  The quality is only available for dall-e-3. <br>


- The **resolution** is the resolution of the image. <br>
  The available resolutions are 256x256, 512x512, 1024x1024, 1024x1792, and 1792x1024. <br>
  The resolution 1024x1024 is available for all models. <br>
  The resolution 256x256 and 512x512 are only available for dall-e-2. <br>
  The resolution 1024x1792 and 1792x1024 are only available for dall-e-3. <br>


- The **style** is the style of the image. <br>
  The available styles are vivid and natural. <br>
  The style is only available for dall-e-3. <br>
  The default style is vivid. <br>


### Speech Configuration

- The **ttsModel** is the model that the bot will use to generate the speech. <br>
  The available models are: <br>

| **Model** | **Pricing**            | 
|:----------|:-----------------------|
| tts-1     | $15.00 / 1M characters |
| tts-1-hd  | $30.00 / 1M characters |

- The **voice** is the voice that the bot will use to generate the speech. <br>
  The available voices are alloy, echo, fable, onyx, nova, and shimmer. <br>


- The **format** is the format of the audio file. <br>
  The available formats are mp3, opus, aac, flac, wav, and pcm. <br>


- The **speed** is the speed of the speech. <br>
  The min value is 0.25 and the max value is 4, the default value is 1. <br> <br>


### Transcription Configuration

- The **transcriptionModel** is the model that the bot will use to generate the transcription. <br>
  The available models are: <br>

| **Model** | **Pricing**                                     |
|:---------:|:------------------------------------------------|
| whisper-1 | $0.006 / minute (rounded to the nearest second) |


- The **prompt** is the prompt that the model will use to generate the transcription. <br>


- The **language** is the language of the audio. <br>


- The **temperature** is the randomness of the transcription. <br>
  Lowering results in less random completions. As the temperature approaches zero, the model will become deterministic
  and repetitive. <br>
  Higher temperature results in more random completions. <br>
  The min value is 0 and the max value is 2. <br> <br>


### 7. Add assets to the database

For the fact, insult and joke command you need to add the assets to the database. <br>
You can import the existing ones to the database from the .tsv files in the ```/storage/assets/``` folder. <br> <br>
You can also add your own assets to the database, just pay attention to the format. <br>

### 8. Compile the bot

After you compiled the bot into a .jar file, you can run it using the following command: <br>
```bash
java -jar NAME_OF_THE_JAR_FILE.jar
``` 

If you use the downloaded jar file to generate example files, you can run it using the following command: <br>
```bash
java -jar YEPPBot.jar -generate
```

After you edited the config files and put in the API keys, you can run the bot using the following command: <br>
```bash
java -jar YEPPBot.jar -botconfig "/PATH/TO/BotConfig.json" -mysqlconfig "/PATH/TO/mySQL.json
```

and so on for the other config files. <br>

You don't need a channel list file, but it's recommended to use one. <br>
You can use the:
- ```-help``` argument to get a list of all the arguments. <br>
- ```-version``` argument to get the version of the bot. <br>
- ```-cli``` argument to run the bot in the command line interface mode. <br>
- ```-nolog``` argument to disable the database logging. <br>

For the configs you can use: <br>
- ```-botconfig "/PATH/TO/BotConfig.json"``` argument to specify the path to the BotConfig.json file. <br>
- ```-channellist "/PATH/TO/Channel.list"``` argument to specify the path to the Channel.list file. <br>
- ```-mysqlconfig "/PATH/TO/mySQL.json"``` argument to specify the path to the mySQL.json file. <br>
- ```-httpsserver "/PATH/TO/httpsserver.json"``` argument to specify the path to the httpserver.json file. <br>
- ```-apiconfig "/PATH/TO/apiKeys.json"``` argument to specify the path to the apiKeys.json file. <br>
- ```-openaiconfig "/PATH/TO/ChatGPT.json"``` argument to specify the path to the ChatGPT.json file. <br>


## Usage and commands

The bot has a variety of commands that you can use. <br>
You can use the:
- ```!help``` command to get a list of all the commands. <br>
- ```!help COMMAND_NAME``` to get more information about a specific command. <br>
- ```!moderate join/leave``` to make the bot join or leave your channel. <br>
- ```!moderate block/unblock``` to blacklist commands. <br>
- ```!counter``` command to create and manage counter commands. <br> <br>

You can use the ```!CustomCommand``` command to create and manage custom commands. <br>
You can also use variables in the custom commands. <br> 
- %author% - will be replaced with the username of the person who executes the command <br>
- %channel% - will be replaced with the channel name where the command was executed <br>
- %tagged% - will be replaced by the first word after the command <br>
- %random% - will be replaced by a random percentage between 0 and 100 <br> <br>

## YEPPConnect

The YEPPConnect is a feature that allows your viewers to whitelist their Minecraft username. <br>
You can use the ```!whitelist add/remove McName``` command to whitelist yourself or remove a user from the whitelist. <br>

On the Minecraft Server you need to install the [YEPPConnect](https://github.com/MCmoderSD/YEPPConnect) plugin. <br>
It's a simple Spigot plugin that connects to the bot and allows the bot to whitelist users. <br>
You can also use the plugin to get the online status of the bot. <br>
The plugin works with Minecraft 1.13 and above, but requires YEPPConnect v1.21.0 to work. <br>
You can find the documentation for the plugin [here](https://github.com/MCmoderSD/YEPPConnect?tab=readme-ov-file#commands-and-permissions). <br> <br>

## Contributing

If you have any ideas or suggestions, feel free to open an issue or a pull request. <br>
You can also contact me on [Discord](https://www.mcmodersd.de/dc) or [Twitch](https://www.twitch.tv/mcmodersd).

Lot of thanks to [Twitch4J](https://twitch4j.github.io/) for the amazing library and
to [OpenAI](https://platform.openai.com/signup) for the ChatGPT API. <br>
Also thanks to [OpenWeatherMap](https://openweathermap.org/api) for the weather API. <br>

Especially thanks to [FoxxHimself](https://github.com/lennartfu) for the original bot and the idea to rewrite it in Java. <br>
Lot of thanks to [RedSmileTV](https://github.com/redsmiletv) and [Rebix](https://github.com/reebix) for the help with the bot and api's and libraries. <br>