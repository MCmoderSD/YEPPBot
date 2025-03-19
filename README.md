# [YEPPBot -  YEPP it's a Twitch Bot](https://www.Twitch.tv/YEPPBotV2)

<br>

## Description
YEPPBot is a comprehensive Twitch bot featuring numerous tools to entertain and manage your Twitch channel.

Originally conceived by [FoxxHimself](https://GitHub.com/lennartfu) and written in Python in early 2021. <br>
I took over the project after the original creator ceased development and rewrote it using Java 21 with the [Twitch4J](https://Twitch4J.GitHub.io/) library. <br>

<br>

## Usage
You can explore the bot's features live on its [Twitch chat](https://www.Twitch.tv/YEPPBotV2/chat). <br>
To integrate it with your channel, use the command `!mod join`. <br>
If you ever want to remove it, simply use `!mod leave`. <br>

For optimal functionality, it's necessary to authenticate the bot with your Twitch account via `!mod auth`. <br>
While not mandatory, assigning the bot a moderator role is advised for all features to work as intended. <br>

<br>

## Contact and Support
For support or inquiries, reach out via [Discord](https://www.mcmodersd.de/dc), [mail](mailto:business@mcmodersd.de), or [Twitch](https://www.Twitch.tv/mcmodersd). <br>
I typically respond within 24 hours, oftentimes sooner.

Interested in collaboration or a custom version tailored to your needs? Simply email [business@mcmodersd.de](mailto:business@mcmodersd.de).

Note that the bot is under active development, and some features may change considerably in the future. <br>
Consequently, the setup process may also evolve. <br>

<br>

## Host Your Own Instance
You can use this project as a template to create your own bot or to host an instance. <br>
You have three options to host and run the bot:
- [Docker](#docker)
- [Precompiled JAR](#precompiled-jar)
- [Manual Setup](#manual-setup)

Regardless of your chosen method, you'll need to prepare configuration files according to the [Configuration](#configuration) section instructions. <br>
You can generate example configuration files using the `-generate` argument.

<hr>

### Docker
You can run the bot within a Docker container. <br>
The Docker image is available on [Docker Hub](https://hub.docker.com/r/mcmodersd/yeppbot). <br>
Make sure to mount your configuration files to the container at `/app/config`.

<hr>

### Precompiled JAR
Ensure that Java 21 is installed on your system, which is available [here](https://www.oracle.com/java/technologies/downloads/#java21). <br>
Download the latest version from the [releases](https://GitHub.com/MCmoderSD/YEPPBot/releases/latest) page. <br>
Run the precompiled JAR, providing absolute paths to the required configuration files:
```bash
java -jar YEPPBot.jar -bot "/path/to/bot.json" -sql "/path/to/sql.json" -server "/path/to/server.json" ...
```

Arguments for configuration files include:
- `-bot "/path/to/bot.json"`: Specifies the bot configuration file path. **(required)**
- `-channels "/path/to/channels.txt"`: Specifies the channel list file path. **(optional)**
- `-sql "/path/to/sql.json"`: Specifies the MariaDB configuration file path. **(required)**
- `-server "/path/to/server.json"`: Specifies the HTTPS server configuration file path. **(required)**
- `-api "/path/to/api.json"`: Specifies the API keys configuration file path. **(optional)**
- `-openai "/path/to/openai.json"`: Specifies the ChatGPT configuration file path. **(optional)**

Other available arguments:
- `-help`: Lists all available arguments.
- `-version`: Displays the current version of the bot.
- `-cli`: Runs the bot in command-line mode.
- `-nolog`: Disables database logging.
- `-noninteractive`: Disables interactive mode.
- `-container`: Enables Docker container mode.
- `-generate`: Produces example configuration files.

<hr>

### Manual Setup
To manually compile and run the bot, first, install Java 21 as detailed [here](https://www.oracle.com/java/technologies/downloads/#java21). <br>
Clone the project into your IDE and organize the configuration files:
- `bot.json`, `channels.txt`, and `server.json` should be in `/src/main/resources/config/`.
- `sql.json` should be in `/src/main/resources/database/`.
- `api.json` and `openai.json` should be in `/src/main/resources/api/`.

Build and run the project following setup. <br>

<br>

## Configuration
Before setting up: <br>

1. Create a Twitch app through the [Twitch Developer Console](https://dev.twitch.tv/console/apps).
2. Establish a Twitch account for the bot (using your own is viable but not ideal).
3. Set up a MariaDB database and user for the bot.

Account tokens may become invalid over time or if the password changes. <br>
Remember **never share or post the token**. <br>

<br>

### Bot Configuration
Create `bot.json` with this format:
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
- **botId**: Retrieve the Twitch user ID for the bot account using [this converter](https://www.streamweasels.com/tools/convert-twitch-username-to-user-id/).
- **botName**: The bot's account name, allowing multiple recognitions separated by commas.
- **botToken**: Generate your bot account's token [here](https://twitchapps.com/tmi/).
- **clientId** and **clientSecret**: Details from your Twitch app.
  - Create the application in the [Twitch Developer Console](https://dev.twitch.tv/console/apps).
  - Set `https://localhost:PORT/callback` for OAuth redirect or your server's domain.
  - Opt for **Chatbot** category and **Confidential** for the client type.
- **prefix**: Symbols used to trigger commands.
- **admins**: Users allowed admin command execution. Admins can use all commands across channels.

<br>

### Channel List (optional)
Create `channels.txt` in this format:
```plaintext
CHANNEL_NAME
OTHER_CHANNEL_NAME
```

List as many channels as needed. <br>
Without this list, the bot will function only in its own channel. <br>

<br>

### MariaDB Database
You must set up a MariaDB database for data management. <br>
The bot auto-generates necessary tables. <br>

Set up MariaDB using:
```sql
CREATE DATABASE DATABASE_NAME;
CREATE USER 'USER_NAME'@'%' IDENTIFIED BY 'USER_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE_NAME.* TO 'USER_NAME'@'%';
FLUSH PRIVILEGES;
```
If the bot and database share a server, replace `%` with `localhost`. <br>

Create `sql.json` like:
```json
{
  "host": "localhost",
  "port": "3306",
  "database": "DATABASE_NAME",
  "username": "USER_NAME",
  "password": "USER_PASSWORD"
}
```
- **host**: Hostname/IP where MariaDB is hosted, e.g., `localhost`.
- **port**: Default MariaDB listening port (`3306`).
- **database**: The bot's database name.
- **username**: The MariaDB user with database access.
- **password**: Password for the MariaDB user.

Ensure the user has full permissions on the database. <br>
Use `-nolog` to disable logging in the database. <br>

<br>

### HTTPS Server
The bot includes an integrated HTTPS server for Twitch Helix API authentication and forthcoming features. <br>
Set up `server.json` like:
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
- `hostname`: Bind server to hostname/IP (e.g., `localhost` or public domain).
- `port`: Server port number (default: `443`).
- `SSL`: For Docker users, mount certificate file directory.
  - `fullchain`: Path to SSL/TLS certificate chain file (`.pem`).
  - `privkey`: Path to corresponding private key file.
- `JKS` (Java KeyStore): Option to use JKS for SSL over `.pem`.
  - `file`: Path to Java KeyStore file.
  - `password`: Java KeyStore password.
  - `validity`: Days the certificate is valid.
  - `CN`: Common Name (this might be your domain name).
  - `OU`, `O`, `L`, `ST`, `C`: Certificate organizational and geographic details.

A valid certificate is required for domains. Self-signed certificates are for `localhost` only. <br>
Obtain a free certificate from [Let's Encrypt](https://LetsEncrypt.org/). <br> 

<br>

### API Keys (optional)
Configure `api.json` for API keys:
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
No need to include unused APIs. <br>

#### API Key Resources:
- **Astrology API**: Currently for horoscopes. Obtain from [Prokerala](https://api.prokerala.com/).
- **OpenWeatherMap API**: Powers weather-related commands. Obtain from [OpenWeatherMap](https://OpenWeatherMap.org/api).
- **Giphy API**: GIF searching functionality. Obtain from [Giphy](https://developers.giphy.com/).
- **Riot API**: For League of Legends data. Obtain from [Riot Games](https://developer.riotgames.com/).

<br>

### OpenAI API (optional)
OpenAI API setup, required for some commands, is optional. <br>
Configure `openai.json` as follows:
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
    "n": 1,
    "maxTokens": 120,
    "instruction": "You are the best TwitchBot that ever existed!",
    "spendingLimit": 32768,
    "priceFactor": 0.25
  }
}
```
To retrieve your API key, visit [OpenAI's Website](https://platform.openai.com/signup). <br>
Fields regarding user, organization, and project IDs are non-mandatory and can be accessed on the [OpenAI dashboard](https://platform.openai.com/settings/organization/general). <br>

### Chat Configuration
| **Field**        | **Description**                                                                      |
|:-----------------|:-------------------------------------------------------------------------------------|
| model            | Model for generating text. (Default: `chatgpt-4o-latest`)                            |
| temperature      | Controls randomness: `0` (deterministic) to `2` (creative). (Default: `1`)           |
| maxOutputTokens  | Maximum tokens in a response. 500 characters ≈ 125 tokens). (Recommended: `120`)     |
| topP             | Nucleus sampling: `0` (plain) to `1` (creative). (Default: `1`)                      |
| frequencyPenalty | Reduces word repetition. Range: `0` to `2`. (Default: `0`)                           |
| presencePenalty  | Discourages repeating words from the conversation. Range: `0` to `2`. (Default: `0`) |
| devMessage       | Provides guidance and instructions for the bot's behavior.                           |
| spendingLimit    | Effective token spending limit before chat resets. (Recommended: `32768`)            |
| priceFactor      | Price factor between input and output token price. (model-dependent)                 |

<br>

## Commands and Features
This bot includes various commands to enhance functionality and interaction. <br>
Below is an overview of key commands:

### Help Commands
- `!help commands`: Displays a list of all available commands.
- `!help COMMAND_NAME`: Provides detailed information about a specific command.

### Moderation Commands
- `!moderate join`: Adds the bot to your channel.
- `!moderate leave`: Removes the bot from your channel.
- `!moderate block COMMAND_NAME`: Blocks a specific command from being used.
- `!moderate unblock COMMAND_NAME`: Unblocks a previously blocked command.

### Counter Commands
- `!counter`: Manages counters for various purposes.
- `!counter add COUNTER_NAME`: Adds a new counter.
- `!counter remove COUNTER_NAME`: Deletes an existing counter.
- `!counter show COUNTER_NAME`: Displays the current value of a counter.
- `!counter set COUNTER_NAME VALUE`: Sets the counter's value.
- `!counter reset COUNTER_NAME`: Resets the counter to its initial value.

### Custom Commands
Create and manage custom commands using `!cc`:
- `!cc add COMMAND_NAME ALIAS: RESPONSE`: Adds a new custom command.
- `!cc remove COMMAND_NAME`: Deletes a custom command.
- `!cc enable COMMAND_NAME`: Enables a disabled custom command.
- `!cc disable COMMAND_NAME`: Disables an enabled custom command.
- `!cc list`: Lists all custom commands.

Dynamic variables can be used within custom commands for interactivity:
- `%author%`: Replaced with the username of the command executor.
- `%channel%`: Replaced with the name of the channel.
- `%tagged%`: Replaced with the first word following the command. **(useful for mentions)**
- `%random%`: Replaced with a random percentage between 0 and 100.

### Info Commands
- `!info moderator`: Lists all moderators in the channel.
- `!info editor`: Lists all editors in the channel.
- `!info vip`: Lists all VIPs in the channel.

### Birthday Commands
- `!birthday set DD.MM.CCYY`: Sets your birthday.
- `!birthday get USERNAME`: Retrieves a user's birthday.
- `!birthday next AMOUNT`: Shows upcoming birthdays.
- `!birthday in USERNAME TIME_FORMAT`: Retrieves information on a user's birthday using a specific time format.
- `!birthday list`: Lists all birthdays.

### ChatGPT Commands
- `!prompt MESSAGE`: Generates a response from the prompt.
- `!chat MESSAGE`: Starts or continues a conversation with the bot.
- `!chat reset`: Resets the current conversation.

### Seasonal Event Commands
Similar commands apply for both the No Nut November and Dick Destroy December events:
- `!NoNutNovember join`: Joins the No Nut November event.
- `!NoNutNovember leave`: Leaves the No Nut November event.
- `!NoNutNovember list`: Lists participant statuses.
- `!NoNutNovember status USERNAME`: Checks a user's status.

### Quote Commands
- `!quote QUOTE_ID`: Retrieves a specific or random quote. **(QUOTE_ID optional)**
- `!quote add QUOTE`: Adds a new quote.
- `!quote remove QUOTE_ID`: Deletes a quote.
- `!quote edit NEW_QUOTE QUOTE_ID`: Edits an existing quote.
- `!quote last`: Retrieves the last quote added.

### Shoutout Commands
- `!so USERNAME`: Performs a shoutout for a specified user.
- `!so enable`: Enables automatic shoutouts for raids.
- `!so disable`: Disables automatic shoutouts for raids.

### General Commands
- `!fact en/de`: Provides a random fact in English or German. **(language optional)**
- `!gif theme`: Searches for a GIF based on the provided theme. **(theme optional)**
- `!horoscope USERNAME LANGUAGE`: Provides a user's daily horoscope. **(language optional)**
- `!insult USERNAME`: Insults the specified user.
- `!joke en/de`: Tells a random joke in English or German. **(language optional)**
- `!lurk`: Tracks and informs how long a user has been lurking.
- `!match AMOUNT LANGUAGE`: Shows compatible users based on zodiac sign. **(amount & language optional)**
- `!ping`: Checks the bot's latency.
- `!riot USERNAME#TAG REGION`: Provides info about a League of Legends player. **(region optional)**
- `!say MESSAGE`: Makes the bot say the specified message. **(admin only)**
- `!status`: Displays the bot's status.
- `!translate LANGUAGE, TEXT`: Translates text to the specified language.
- `!weather LOCATION, LANGUAGE`: Provides current weather information for a location. **(language optional)**
- `!wiki en/de SEARCH_TERM`: Searches Wikipedia for the term. **(language optional)**

<hr>

### CLI Commands
- `exit`: Shuts down the bot.
- `clear`: Clears the console.
- `help`: Lists all available CLI commands.
- `uptime`: Displays the bot's uptime.
- `generate`: Generates example configuration files.

<br>

## Planned Features
- **YEPPConnect**: A Minecraft plug-in for whitelist management and Twitch integration.
- **Key Command**: Locating the least expensive game key.
- **Custom Timer**: Setting timed messages.
- **Rank**: Displays a user's rank in a game.
- **Social**: Shows a streamer's or user's social media.
- **Web UI**: A web interface for bot management.
- **Discord Integration**: Integration with Discord for cross-platform functionality.

<br>

## Contributing
Have ideas or feedback? Feel free to:
- Submit issues or requests on GitHub.
- Reach me on [Discord](https://www.mcmodersd.de/dc) or [Twitch](https://www.twitch.tv/mcmodersd).

<br>

## Acknowledgments
Special thanks to:
- [FoxxHimself](https://github.com/lennartfu): For initial bot creation and inspiration for the Java transition.
- [RedSmileTV](https://github.com/redsmiletv): For API and library help.
- [Rebix](https://github.com/reebix): For guidance and support.
- [Xander](https://github.com/Xander1233): For assistance with Docker, CI/CD and pen-testing.
- [r4kunnn](https://www.twitch.tv/r4kunnn): For testing and feedback.

Your contributions and support are greatly appreciated. <br>
Together, we can make the bot even better!