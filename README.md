# [YEPPBot - YEPP it's a Twitch-Bot](https://www.twitch.tv/YEPPBotV2)

## Table of Contents
- [Description](#description)
- [Features](#features)
- [Installation](#installation-and-setup)
- [Usage and commands](#usage-and-commands)
- [YEPPConnect](#yeppconnect)
- [Contributing](#contributing)


## Description
The YEPPBot is a Twitch-Bot that provides a variety of features to entertain and manage your Twitch channel. <br>

The original idea comes from [FoxxHimself](https://github.com/lennartfu) and was written in Python back in the first half of 2021. <br>
After the original creator stopped working on and maintaining the bot, I took over its maintenance and decided to rewrite it in Java.
The bot has since been fully rewritten in Java 21 using the [Twitch4J](https://twitch4j.github.io/) library. <br>

You can try out the bot on [Twitch](https://www.twitch.tv/YEPPBotV2/chat) to see its features in action. <br>
To use the bot in your own channel, you can add it to your channel using the `!mod join` command. <br>
If you wish to remove the bot from your channel, use the `!mod leave` command in your channel. <br> <br>

For all features to work as intended, you need to authenticate the bot with your Twitch account using the `!mod auth` command in your channel. <br>
While it's recommended to assign the bot a moderator role in your channel to unlock all features, this is not strictly necessary. <br> <br>

If you need help or have any questions, feel free to reach out via [Discord](https://www.mcmodersd.de/dc), [mail](mailto:business@mcmodersd.de) or [Twitch](https://www.twitch.tv/mcmodersd). <br>
I typically respond within 24 hours, often much faster. <br>

If you're interested in collaborating or need a customized version to suit your needs? <br>
Simply email [business@mcmodersd.de](mailto:business@mcmodersd.de). <br> <br>

The bot is currently under active development, and some features may undergo significant changes in the future. <br>
So please note that the setup process for the bot may also change over time. <br>

<hr>

You can use this project as a template to create your own bot or to host your own instance of it. <br>
Setting up the bot is relatively straightforward: you need to create the configuration files and input the required API keys. <br>

However, it is necessary to create a [Twitch application](https://dev.twitch.tv/console) and provide access to a MySQL database. <br>
For ChatGPT features, as well as certain other features, you need to provide their respective API keys for them to function properly. <br>

If you have any ideas or suggestions, feel free to open an issue or submit a pull request. <br> <br>


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
  - [ ] Rank Command
  - [ ] Key Command
  - [ ] Discord Integration


## Installation and Setup
For the bot to work, you need to have a few things set up:
- You need to have Java 21 installed on your computer a download link can be found [here](https://www.oracle.com/uk/java/technologies/downloads/#java21).
- You need the Twitch account token of the Twitch bot which you can get from [here](https://twitchapps.com/tmi/).
- And you need to register an application on the [Twitch Developer Console](https://dev.twitch.tv/console/apps).

Keep in mind that the account token can become invalid after a while and if you change the password of the account. <br>
But remember **DON'T POST** or ever **SHARE** the token anywhere!!! <br> <br>


### 1. Download the bot jar file
You can download the latest version of the bot from the [releases](https://github.com/MCmoderSD/YEPPBot/releases/latest) page. <br>
Create the configuration files yourself, or use the `-generate` argument to create example files. <br>

YAlternatively, you can clone the repository and compile the bot yourself.
To do this, ensure you have Git installed on your computer. You can download it from [here](https://git-scm.com/downloads). <br>

Clone the repository using the following command: <br>
```bash
git clone https://www.github.com/MCmoderSD/YEPPBot.git 
``` 
<br>


### 2. Edit the config files
You need to fill in the configuration files with the required information. <br> 
Ignore the names or paths if you use the precompiled JAR file. <br>

You must create two JSON files in the `/src/main/resources/config/` folder. <br>
The first file is `BotConfig.json` and should have the following structure: <br>
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
- **botId**: The ID of the bot account, which you can obtain using this [username to id converter](https://www.streamweasels.com/tools/convert-twitch-username-to-user-id/).
- **botName**: The name of the bot account. You can use multiple names separated by commas so the bot can recognize when it's mentioned.
- **botToken**: The token of the bot account, which you can generate [here](https://twitchapps.com/tmi/).
- **clientId** and **clientSecret**: The credentials for your Twitch application. 
  - You need to create an application in the [Twitch Developer Console](https://dev.twitch.tv/console/apps). 
  - For testing purposes, use `https://localhost:PORT/callback` as the OAuth Redirect URL. <br> For production, ensure the URL points to your server.
  - Set the application category to **Chatbot** and the Client Type to **Confidential**.
- **prefix**: The character(s) the bot will use to recognize commands.
- **admins**: The users who will have access to admin commands. <br>

The prefix is the character that the bot will use to recognize commands. <br>
The admins are the users that have access to the admin commands. <br>

The second file is optional and is called `Channel.list`. It should have the following structure: <br>
```
CHANNEL_NAME
OTHER_CHANNEL_NAME
``` 
You can add as many channels as you like. 
If no channel list is provided, the bot will default to joining its own channel. <br> <br>


### 3. MySQL Database
To run and use the bot, you need to have your own MySQL database. <br> 
The bot will automatically create the necessary tables and handle the setup, but you must provide a configuration file. <br>

Create a file named `mySQL.json` in the `/src/main/resources/database/`folder. <br> 
The file should follow this structure:
```json
{
  "host": "localhost",
  "port": "3306",
  "database": "DATABASE_NAME",
  "username": "USER_NAME",
  "password": "USER_PASSWORD"
}
```

- **host**: The hostname or IP address of your MySQL server (e.g., `localhost`). 
- **port**: The port your MySQL server is listening on (default is `3306`).
- **database**: The name of the database the bot will use.
- **username**: The MySQL user with access to the database.
- **password**: The password for the MySQL user.

Ensure that the specified user has full permissions over the entire database. <br>
If you don't want to use the database logging, you can use the `-nolog` argument. <br> <br>


### 4. HTTPS Server
The YEPPBot includes a simple HTTPS server to handle Helix API authentication. <br> 
It can also be used to broadcast sound via a browser page. <br>

To configure the server, create a file named `httpsServer.json` in the `/src/main/resources/config/` folder. <br> 
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

#### Key Configuration Notes:
- **hostname**: Specify your server's hostname or use `localhost` for local testing. 
- **port**: Set the port for the HTTPS server.
- **keystore**: Path to your Java KeyStore (JKS) file. 
- **fullchain**: Path to the certificate chain file (for non-JKS setups). 
- **privkey**: Path to the private key file (for non-JKS setups).

#### Localhost and Self-Signed Certificates:
You can use localhost with a self-signed JKS file for testing. However:
- The Twitch API will work with self-signed certificates.
- OBS will not accept self-signed certificates for the browser source.

#### Generating a Self-Signed Certificate:
To create a self-signed JKS file, use the following command:
```bash
keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 360 -keysize 2048
```
- Set the **password** to the bot token, hashed with **SHA-256** and encoded in **Base64**.
- Use this [SHA-256 hash tool](https://emn178.github.io/online-tools/sha256.html) to generate the hash.
 - Ensure **UTF-8** is selected as the input encoding. 
 - Set **Base64** as the output format.

#### Using a Domain and Valid Certificates:
If you have a domain, you must use a valid certificate. Self-signed certificates are only allowed for `localhost`.
You can obtain a free certificate from [Let's Encrypt](https://letsencrypt.org/). <br> <br>


### 5. Add API Keys
To configure the bot with API keys, create a file named `apiKeys.json` in the `/src/main/resources/api/` folder. <br> 
The file should follow this structure:
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
If you do not wish to use a specific API or do not have the corresponding API key, you can omit that part of the configuration.

#### API Key Sources:
- **Astrology API**: Currently only used for the horoscope feature. Obtain your key from [Prokerala](https://api.prokerala.com/).
- **OpenWeatherMap API**: Used for the weather command. Obtain your key from [OpenWeatherMap](https://openweathermap.org/api).
- **Giphy API**: Used for GIF integration. Obtain your key from [Giphy](https://developers.giphy.com/). <br> <br>


### 6. Add a ChatGPT API key
You only need to configure this module if you plan to use it. 
For example, if you don’t want to use the image module, you can omit the image part of the configuration. <br>

To set up, create a file named `ChatGPT.json` in the `/src/main/resources/api/` folder. <br> 
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
You can obtain your API key from [OpenAI](https://platform.openai.com/signup). <br>

<hr>

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
  The limit is 16,383 tokens, but it's recommended to use a value that is suitable for the use, on Twitch the message
  limit is 500 characters.
  If you divide the limit by 4, you are an estimate the number of characters. <br>


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

<hr>

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

<hr>

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

<hr>

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
java -jar YEPPBot.jar -botconfig "/PATH/TO/BotConfig.json" -mysql "/PATH/TO/mySQL.json" -httpsserver "/PATH/TO/httpsserver.json"
```
You can include additional arguments for other configuration files as needed.

<hr>

#### General Arguments:
- `-help`: Displays a list of all available arguments. 
- `-version`: Outputs the current version of the bot. 
- `-cli`: Runs the bot in command-line interface mode. 
- `-nolog`: Disables database logging.


#### Configuration File Arguments:
- `-botconfig "/PATH/TO/BotConfig.json"`: Specifies the path to the `BotConfig.json` file. 
- `-channellist "/PATH/TO/Channel.list"`: Specifies the path to the `Channel.list` file. 
- `-mysql "/PATH/TO/mySQL.json"`: Specifies the path to the `mySQL.json` file. 
- `-httpsserver "/PATH/TO/httpsServer.json"`: Specifies the path to the `httpsServer.json` file. 
- `-api "/PATH/TO/apiKeys.json"`: Specifies the path to the `apiKeys.json` file.
- `-openai "/PATH/TO/ChatGPT.json"`: Specifies the path to the `ChatGPT.json` file. <br> <br>


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