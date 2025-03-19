# [YEPPBot -  YEPP it's a Twitch Bot](https://www.Twitch.tv/YEPPBotV2)

<br>

## Description
YEPPBot is a Twitch bot equipped with multiple features to entertain and manage your Twitch channel.

Originally conceived by [FoxxHimself](https://GitHub.com/lennartfu) and written in Python in early 2021. <br>
I took over the project after the original creator ceased development and rewrote it using Java 21 with the [Twitch4J](https://Twitch4J.GitHub.io/) library. <br> 

<br>

## Usage
Experience the bot by visiting its [Twitch chat](https://www.Twitch.tv/YEPPBotV2/chat) to see its features in action. <br>
Add the bot to your channel with the command `!mod join`. <br>
Should you wish to remove it, use `!mod leave` in your channel.

For all features to work as intended, you need to authenticate the bot with your Twitch account using `!mod auth` in your channel. <br>
It's recommended to assign the bot a moderator role, although it isn't strictly necessary. <br> 

<br>

## Contact and Support
For support or inquiries, reach out via [Discord](https://www.mcmodersd.de/dc), [mail](mailto:business@mcmodersd.de), or [Twitch](https://www.Twitch.tv/mcmodersd). <br>
I typically respond within 24 hours, oftentimes sooner.

Interested in collaboration or a custom version tailored to your needs? Simply email [business@mcmodersd.de](mailto:business@mcmodersd.de). 

Note that the bot is under active development, and some features may change considerably in the future. <br>
Consequently, the setup process may also evolve. <br> 

<br>

## Host Your Own Instance
Utilize this project as a template to create your own bot or to host an instance. <br>
You have three options to run the bot:
- [Docker](#docker)
- [Precompiled JAR](#precompiled-jar)
- [Manual Setup](#manual-setup)

Regardless of the method, you must create the configuration files following the [Configuration](#configuration) section guidelines. <br>
Use the `-generate` argument to create example configuration files as templates.

<hr>

### Docker
Run the bot in a Docker container. The Docker image is available on [Docker Hub](https://hub.docker.com/r/mcmodersd/yeppbot). <br>
Mount your configuration files to the container at `/app/config`.

<hr>

### Precompiled JAR
Ensure Java 21 is installed on your computer, you can download it from [here](https://www.oracle.com/java/technologies/downloads/#java21). <br>
Download the latest version from the [releases](https://GitHub.com/MCmoderSD/YEPPBot/releases/latest) page. <br>
Execute the precompiled JAR, specifying the absolute paths to the configuration files:
```bash
java -jar YEPPBot.jar -bot "/path/to/bot.json" -sql "/path/to/sql.json" -server "/path/to/server.json" and so on...
```

Arguments for configuration files include:
- `-bot "/path/to/bot.json"`: Specifies the bot configuration file path. **(required)**
- `-channels "/path/to/channels.txt"`: Specifies the channel list file path. **(optional)**
- `-sql "/path/to/sql.json"`: Specifies the MariaDB configuration file path. **(required)**
- `-server "/path/to/server.json"`: Specifies the HTTPS server configuration file path. **(required)**
- `-api "/path/to/api.json"`: Specifies the API keys configuration file path. **(optional)**
- `-openai "/path/to/openai.json"`: Specifies the ChatGPT configuration file path. **(optional)**

Other available arguments:
- `-help`: Displays a list of all available arguments.
- `-version`: Outputs the current version of the bot.
- `-cli`: Runs the bot in command-line interface mode.
- `-nolog`: Disables database logging.
- `-noninteractive`: Disables interactive mode.
- `-container`: Activates Docker container mode.
- `-generate`: Generates example configuration files.

<hr>

### Manual Setup
Compile and run the bot manually by first installing Java 21. Download Java 21 [here](https://www.oracle.com/java/technologies/downloads/#java21). <br>
Clone the project into your IDE and place the following configuration files in `/src/main/resources/`:
- Put `bot.json`, `channels.txt`, and `server.json` in `/src/main/resources/config/`.
- Put `sql.json` in `/src/main/resources/database/`.
- Put `api.json` and `openai.json` in `/src/main/resources/api/`.

Build and run the project thereafter. <br> 

<br>

## Configuration
Before starting configuration:
1. Create a Twitch application in the [Twitch Developer Console](https://dev.twitch.tv/console/apps).
2. Create a Twitch account for the bot. (Your own account works, but is not recommended).
3. Generate a MariaDB database and user for the bot.

Account tokens may become invalid over time or if the password changes. <br> 
Remember **never share or post the token**. <br> 

<br>

### Bot Configuration
Create `bot.json` with this structure:
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
- **botId**: The Twitch user ID of the bot account, obtained via this [username to ID converter](https://www.streamweasels.com/tools/convert-twitch-username-to-user-id/).
- **botName**: The bot account's name. Allow multiple names separated by commas for recognition.
- **botToken**: Generate the bot account's token [here](https://twitchapps.com/tmi/).
- **clientId** and **clientSecret**: Credentials of your Twitch application.
  - Create an application in the [Twitch Developer Console](https://dev.twitch.tv/console/apps).
  - Set `https://localhost:PORT/callback` as the OAuth redirect URL or use a domain pointed to your server.
  - Select **Chatbot** as the category and **Confidential** as the client type.
- **prefix**: Characters for command recognition.
- **admins**: Users with access to admin commands.

The prefix identifies commands. <br>
Admins can execute all commands in any channel. <br> 

<br>

### Channel List (optional)
The optional `channels.txt` file should feature this format:
```
CHANNEL_NAME
OTHER_CHANNEL_NAME
```
List as many channels as desired. <br>
Without this list, the bot is active in its own channel only. <br> 

<br>

### MariaDB Database
A MariaDB database is required for data storage. <br>
The bot creates the necessary tables automatically. <br>

Create a MariaDB database and user with:
```sql
CREATE DATABASE DATABASE_NAME;
CREATE USER 'USER_NAME'@'%' IDENTIFIED BY 'USER_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE_NAME.* TO 'USER_NAME'@'%';
FLUSH PRIVILEGES;
```
If the bot and database run on the same server, use `localhost` in place of `%`.

Create `sql.json` structured like:
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

Ensure the user has complete database permissions. <br>
Use `-nolog` to disable database logging. <br> 

<br>

### HTTPS Server
The bot features a built-in HTTPS server for Twitch Helix API authentication and forthcoming features.

Create `server.json` structured like:
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
Exclude any unutilized APIs.

#### API Key Sources:
- **Astrology API**: Currently only for horoscope. Obtain from [Prokerala](https://api.prokerala.com/).
- **OpenWeatherMap API**: For weather commands. Obtain from [OpenWeatherMap](https://OpenWeatherMap.org/api).
- **Giphy API**: For GIF integration. Obtain from [Giphy](https://developers.giphy.com/).
- **Riot API**: For League of Legends commands. Obtain from [Riot Games](https://developer.riotgames.com/)

<br>

### OpenAI API (optional)
OpenAI API setup is optional but necessary for some commands.
Configure `openai.json` like:
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
- Obtain an API key from [OpenAI](https://platform.openai.com/signup).
- Fields like `user`, `organizationId`, and `projectId` are optional.
- IDs pertain to tracking and are available via the [OpenAI dashboard](https://platform.openai.com/settings/organization/general).

<hr>

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
- `%tagged%`: Replaced with the first word following the command (useful for mentions).
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
- `!quote QUOTE_ID`: Retrieves a specific or random quote (QUOTE_ID optional).
- `!quote add QUOTE`: Adds a new quote.
- `!quote remove QUOTE_ID`: Deletes a quote.
- `!quote edit NEW_QUOTE QUOTE_ID`: Edits an existing quote.
- `!quote last`: Retrieves the last quote added.

### Shoutout Commands
- `!so USERNAME`: Performs a shoutout for a specified user.
- `!so enable`: Enables automatic shoutouts during raids.
- `!so disable`: Disables automatic shoutouts during raids.

### General Commands
- `!fact en/de`: Provides a random fact in English or German (language optional).
- `!gif theme`: Searches for a GIF based on the provided theme (theme optional).
- `!horoscope USERNAME LANGUAGE`: Provides a user's daily horoscope (language optional).
- `!insult USERNAME`: Insults the specified user.
- `!joke en/de`: Tells a random joke in English or German (language optional).
- `!lurk`: Tracks and informs how long a user has been lurking.
- `!match AMOUNT LANGUAGE`: Shows compatible users based on zodiac sign (amount & language optional).
- `!ping`: Checks the bot's latency.
- `!riot USERNAME#TAG REGION`: Provides info about a League of Legends player (region optional).
- `!say MESSAGE`: Makes the bot say the specified message (admin only).
- `!status`: Displays the bot's status.
- `!translate LANGUAGE, TEXT`: Translates text to the specified language.
- `!weather LOCATION, LANGUAGE`: Provides current weather information for a location (language optional).
- `!wiki en/de SEARCH_TERM`: Searches Wikipedia for the term (language optional).

### CLI Commands
- `exit`: Shuts down the bot.
- `clear`: Clears the console.
- `help`: Lists all available CLI commands.
- `uptime`: Displays the bot's uptime.
- `generate`: Generates example configuration files.

## Planned Features
- YEPPConnect: A Minecraft plugin for whitelist management and Twitch integration.
- Key Command: A command to find the cheapest key for a game.
- Custom Timer: A command to set a custom timer for timed messages.
- Rank: A command to fetch the rank of a user in a game.
- Social: A command to fetch the social media of a streamer or user.
- Web UI: A web interface for bot management.
- Discord Integration: Integration with Discord for cross-platform functionality.
- 
<br>

## Contributing
Have an idea or suggestion? Feel free to:
- Open an issue or pull request on GitHub.
- Contact me on [Discord](https://www.mcmodersd.de/dc) or [Twitch](https://www.twitch.tv/mcmodersd).

<br>

## Acknowledgments
Special thanks to:
- [FoxxHimself](https://github.com/lennartfu): For creating the original bot and inspiring its Java rewrite.
- [RedSmileTV](https://github.com/redsmiletv): For assistance with APIs and libraries.
- [Rebix](https://github.com/reebix): For guidance and support.
- [Xander](https://github.com/Xander1233): For Docker, CI/CD, and pen-testing aid.
- [r4kunnn](https://www.twitch.tv/r4kunnn): For rigorous testing and feedback.

Your contributions and support are greatly appreciated. <br>
Together, we can make the bot even better!