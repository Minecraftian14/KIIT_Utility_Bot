# KIIT Utility Bot
 
As the name suggests, this bot is designed to provide some utilities and tools to make working around in our section-private server.

[![](https://img.shields.io/discord/872811194170347520?color=%237289da&logoColor=%23424549)](https://discord.gg/hZnHFGvU6W)

Well... For now it it's just a prototype of a verification bot.

It scanns through all messages (make them private/dm)  for the word verify and a kiit mail id.
If the data matched is valid, an OTP is geerated and sent to the mail.

#### Libraries Used
* [JDA](https://github.com/DV8FromTheWorld/JDA) - Java Discord API. This is used to create a discord bot - it provides the classes and methods to register events and act on them
* [Javax Mail](https://mvnrepository.com/artifact/com.sun.mail/javax.mail/1.6.2) - Java Mail API. This is used to send a mail containing an OTP.
* [MyLOGGER](https://github.com/Minecraftian14/MyLOGGER) - A logger I made to print "stuff" to console.

Note, that the bot only works on one server...

The current prototype considers the following conditions for verification.

* Accepts commands from users only (a bot cant control this bot)
* Ignores users not belonging to this server.
* Acts only when the message contains the word "verify" and a kiit mail address.
* The mail provided should start from 2105
* The later part must be a valid roll number. 

---

To test run it on your system, I recommend you to use Intellij IDEA and import this project as a gradle project.

Further, one must create a file named `env.properties` located at `src/main/resources` with the following content
```js
bot_token=YOUR DISCORD BOT TOKEN
invite_link=NOT REALLY USED ANYWHERE
mail_from=SENDER'S MAIL ID
app_pass=GMAIL APPLICATION PASSWORD
user_name=YOUR USER NAME
valid_roll_calls=A LIST OF COMMA SEPARATED 8 DIGIT ROLL NUMBERS
```

Click the following links to get the required data elements:
* [`bot_token`](https://www.writebots.com/discord-bot-token/#:~:text=Generating%20Your%20Token%20Step%2Dby%2DStep)
* [`invite_link`](https://www.writebots.com/discord-bot-token/#:~:text=Add%20Your%20Bot%20to%20a%20Discord%20Server)
* [`mail_from`](https://www.lifewire.com/what-is-my-email-address-4143261)
* [`app_pass`](https://support.google.com/mail/answer/185833?hl=en-GB)

Extra information about other fields:
* `user_name` - I think can almost be anything, feel free to use your name.
* `valid_roll_calls` - For example 21052051,21052052,21052053
