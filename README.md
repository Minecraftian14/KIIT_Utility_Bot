# KIIT Utility Bot
 
As the name suggests, this bot is designed to provide some utilities and tools to make working around in our section-private server.

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
