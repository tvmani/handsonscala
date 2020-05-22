import $file.LoggingPipeline, LoggingPipeline.{logger, cc}

logger.send("I am cow")
logger.send("hear me moo")
logger.send("I weight twice as much as you")
logger.send("And I look good on the barbecue")
logger.send("Yoghurt curds cream cheese and butter")
logger.send("Comes from liquids from my udder")
logger.send("I am cow1234567887654321")
logger.send("Hear me moo, moooo")

cc.waitForInactivity()

def decodeFile(p: os.Path) = {
  os.read.lines(p).map(s => new String(java.util.Base64.getDecoder.decode(s)))
}

assert(decodeFile(os.pwd / "log.txt-old") == Seq("I am cow1234567887654321"))
assert(decodeFile(os.pwd / "log.txt") == Seq("Hear me moo, moooo"))
