# Startup Simulator

A silly little Discord bot.

## How do I host my own?

```Bash
$ docker build -t me/startup-simulator
$ docker run -dit -e BOT_TOKEN="your token here" me/startup-simulator
```

Other env vars can be discovered by reading the source, but they're not very important. 
