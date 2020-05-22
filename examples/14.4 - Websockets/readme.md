```bash
./mill -i app.test
```

Diff from [14.3 - Ajax](https://github.com/handsonscala/handsonscala/tree/master/examples/14.14.3%20-%20Ajax):
```diff
diff --git a/14.3 - Ajax/app/src/MinimalApplication.scala b/14.4 - Websockets/app/src/MinimalApplication.scala
index d165089..f441d0f 100644
--- a/14.3 - Ajax/app/src/MinimalApplication.scala	
+++ b/14.4 - Websockets/app/src/MinimalApplication.scala	
@@ -3,6 +3,8 @@ import scalatags.Text.all._
 object MinimalApplication extends cask.MainRoutes {
   var messages = Vector(("alice", "Hello World!"), ("bob", "I am cow, hear me moo"))
 
+  var openConnections = Set.empty[cask.WsChannelActor]
+
   @cask.get("/")
   def hello() = doctype("html")(
     html(
@@ -21,13 +23,12 @@ object MinimalApplication extends cask.MainRoutes {
               }
             ).then(response => response.json())
              .then(json => {
-              if (json["success"]) {
-                messageList.innerHTML = json["txt"]
-                msgInput.value = ""
-              }
+              if (json["success"]) msgInput.value = ""
               errorDiv.innerText = json["err"]
             })
           }
+          var socket = new WebSocket("ws://" + location.host + "/subscribe");
+          socket.onmessage = function(ev) { messageList.innerHTML = ev.data }
         """))
       ),
       body(
@@ -53,9 +54,17 @@ object MinimalApplication extends cask.MainRoutes {
     else if (msg == "") ujson.Obj("success" -> false, "err" -> "Message cannot be empty")
     else {
       messages = messages :+ (name -> msg)
-      ujson.Obj("success" -> true, "txt" -> messageList().render, "err" -> "")
+      for (conn <- openConnections) conn.send(cask.Ws.Text(messageList().render))
+      ujson.Obj("success" -> true, "err" -> "")
     }
   }
 
+  @cask.websocket("/subscribe")
+  def subscribe() = cask.WsHandler { connection =>
+    connection.send(cask.Ws.Text(messageList().render))
+    openConnections += connection
+    cask.WsActor { case cask.Ws.Close(_, _) => openConnections -= connection }
+  }
+
   initialize()
 }
diff --git a/14.3 - Ajax/app/test/src/ExampleTests.scala b/14.4 - Websockets/app/test/src/ExampleTests.scala
index f085deb..bc63b12 100644
--- a/14.3 - Ajax/app/test/src/ExampleTests.scala	
+++ b/14.4 - Websockets/app/test/src/ExampleTests.scala	
@@ -1,22 +1,28 @@
 package app
 
 import utest._
+import scala.concurrent._, duration.Duration.Inf
+import castor.Context.Simple.global, cask.util.Logger.Console._
 
 object ExampleTests extends TestSuite {
   def withServer[T](example: cask.main.Main)(f: String => T): T = {
     val server = io.undertow.Undertow.builder
-      .addHttpListener(8083, "localhost")
+      .addHttpListener(8084, "localhost")
       .setHandler(example.defaultHandler)
       .build
     server.start()
     val res =
-      try f("http://localhost:8083")
+      try f("http://localhost:8084")
       finally server.stop()
     res
   }
 
   val tests = Tests {
     test("success") - withServer(MinimalApplication) { host =>
+      var wsPromise = scala.concurrent.Promise[String]
+      val wsClient = cask.util.WsClient.connect(s"$host/subscribe") {
+        case cask.Ws.Text(msg) => wsPromise.success(msg)
+      }
       val success = requests.get(host)
 
       assert(success.text().contains("Scala Chat!"))
@@ -26,20 +32,39 @@ object ExampleTests extends TestSuite {
       assert(success.text().contains("I am cow, hear me moo"))
       assert(success.statusCode == 200)
 
+      val wsMsg = Await.result(wsPromise.future, Inf)
+
+      assert(wsMsg.contains("alice"))
+      assert(wsMsg.contains("Hello World!"))
+      assert(wsMsg.contains("bob"))
+      assert(wsMsg.contains("I am cow, hear me moo"))
+
+      wsPromise = scala.concurrent.Promise[String]
       val response = requests.post(host, data = ujson.Obj("name" -> "haoyi", "msg" -> "Test Message!"))
 
       val parsed = ujson.read(response.text())
       assert(parsed("success") == ujson.True)
       assert(parsed("err") == ujson.Str(""))
 
-      val parsedTxt = parsed("txt").str
-      assert(parsedTxt.contains("alice"))
-      assert(parsedTxt.contains("Hello World!"))
-      assert(parsedTxt.contains("bob"))
-      assert(parsedTxt.contains("I am cow, hear me moo"))
-      assert(parsedTxt.contains("haoyi"))
-      assert(parsedTxt.contains("Test Message!"))
       assert(response.statusCode == 200)
+      val wsMsg2 = Await.result(wsPromise.future, Inf)
+      assert(wsMsg2.contains("alice"))
+      assert(wsMsg2.contains("Hello World!"))
+      assert(wsMsg2.contains("bob"))
+      assert(wsMsg2.contains("I am cow, hear me moo"))
+      assert(wsMsg2.contains("haoyi"))
+      assert(wsMsg2.contains("Test Message!"))
+
+      val success2 = requests.get(host)
+
+      assert(success2.text().contains("Scala Chat!"))
+      assert(success2.text().contains("alice"))
+      assert(success2.text().contains("Hello World!"))
+      assert(success2.text().contains("bob"))
+      assert(success2.text().contains("I am cow, hear me moo"))
+      assert(success2.text().contains("haoyi"))
+      assert(success2.text().contains("Test Message!"))
+      assert(success2.statusCode == 200)
     }
     test("failure") - withServer(MinimalApplication) { host =>
       val response1 = requests.post(host, data = ujson.Obj("name" -> "haoyi"), check = false)
```