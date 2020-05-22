```bash
./mill -i app.test
```

Diff from [14.4 - Websockets](https://github.com/handsonscala/handsonscala/tree/master/examples/14.14.4%20-%20Websockets):
```diff
diff --git a/14.4 - Websockets/app/src/MinimalApplication.scala b/14.7 - WebsocketsJsoup/app/src/MinimalApplication.scala
index f441d0f..12aa627 100644
--- a/14.4 - Websockets/app/src/MinimalApplication.scala	
+++ b/14.7 - WebsocketsJsoup/app/src/MinimalApplication.scala	
@@ -46,15 +46,20 @@ object MinimalApplication extends cask.MainRoutes {
     )
   )
 
-  def messageList() = frag(for ((name, msg) <- messages) yield p(b(name), " ", msg))
+  def messageList() = frag(
+    for ((name, msg) <- synchronized(messages))
+    yield p(b(name), " ", msg)
+  )
 
   @cask.postJson("/")
   def postHello(name: String, msg: String) = {
     if (name == "") ujson.Obj("success" -> false, "err" -> "Name cannot be empty")
     else if (msg == "") ujson.Obj("success" -> false, "err" -> "Message cannot be empty")
-    else {
-      messages = messages :+ (name -> msg)
-      for (conn <- openConnections) conn.send(cask.Ws.Text(messageList().render))
+    else synchronized{
+      synchronized{ messages = messages :+ (name -> msg) }
+      for (conn <- synchronized(openConnections)) {
+        conn.send(cask.Ws.Text(messageList().render))
+      }
       ujson.Obj("success" -> true, "err" -> "")
     }
   }
@@ -62,8 +67,10 @@ object MinimalApplication extends cask.MainRoutes {
   @cask.websocket("/subscribe")
   def subscribe() = cask.WsHandler { connection =>
     connection.send(cask.Ws.Text(messageList().render))
-    openConnections += connection
-    cask.WsActor { case cask.Ws.Close(_, _) => openConnections -= connection }
+    synchronized{ openConnections += connection }
+    cask.WsActor { case cask.Ws.Close(_, _) =>
+      synchronized {openConnections -= connection }
+    }
   }
 
   initialize()
diff --git a/14.4 - Websockets/app/test/src/ExampleTests.scala b/14.7 - WebsocketsJsoup/app/test/src/ExampleTests.scala
index bc63b12..a11e656 100644
--- a/14.4 - Websockets/app/test/src/ExampleTests.scala	
+++ b/14.7 - WebsocketsJsoup/app/test/src/ExampleTests.scala	
@@ -3,16 +3,18 @@ package app
 import utest._
 import scala.concurrent._, duration.Duration.Inf
 import castor.Context.Simple.global, cask.util.Logger.Console._
+import org.jsoup._
+import collection.JavaConverters._
 
 object ExampleTests extends TestSuite {
   def withServer[T](example: cask.main.Main)(f: String => T): T = {
     val server = io.undertow.Undertow.builder
-      .addHttpListener(8084, "localhost")
+      .addHttpListener(8086, "localhost")
       .setHandler(example.defaultHandler)
       .build
     server.start()
     val res =
-      try f("http://localhost:8084")
+      try f("http://localhost:8086")
       finally server.stop()
     res
   }
@@ -23,14 +25,18 @@ object ExampleTests extends TestSuite {
       val wsClient = cask.util.WsClient.connect(s"$host/subscribe") {
         case cask.Ws.Text(msg) => wsPromise.success(msg)
       }
-      val success = requests.get(host)
 
-      assert(success.text().contains("Scala Chat!"))
-      assert(success.text().contains("alice"))
-      assert(success.text().contains("Hello World!"))
-      assert(success.text().contains("bob"))
-      assert(success.text().contains("I am cow, hear me moo"))
-      assert(success.statusCode == 200)
+      val jsoupRequest = Jsoup.connect(host)
+      val success = jsoupRequest.get()
+      assert(success.select("h1").asScala.exists(_.text() == "Scala Chat!"))
+      assert(
+        success.select("div#messageList > p").asScala.map(_.text()) ==
+        Seq(
+          "alice Hello World!",
+          "bob I am cow, hear me moo"
+        )
+      )
+      assert(jsoupRequest.response().statusCode() == 200)
 
       val wsMsg = Await.result(wsPromise.future, Inf)
 
@@ -55,16 +61,18 @@ object ExampleTests extends TestSuite {
       assert(wsMsg2.contains("haoyi"))
       assert(wsMsg2.contains("Test Message!"))
 
-      val success2 = requests.get(host)
-
-      assert(success2.text().contains("Scala Chat!"))
-      assert(success2.text().contains("alice"))
-      assert(success2.text().contains("Hello World!"))
-      assert(success2.text().contains("bob"))
-      assert(success2.text().contains("I am cow, hear me moo"))
-      assert(success2.text().contains("haoyi"))
-      assert(success2.text().contains("Test Message!"))
-      assert(success2.statusCode == 200)
+      val jsoupRequest2 = Jsoup.connect(host)
+      val success2 = jsoupRequest2.get()
+      assert(success2.select("h1").asScala.exists(_.text() == "Scala Chat!"))
+      assert(
+        success2.select("div#messageList > p").asScala.map(_.text()) ==
+          Seq(
+            "alice Hello World!",
+            "bob I am cow, hear me moo",
+            "haoyi Test Message!"
+          )
+      )
+      assert(jsoupRequest2.response().statusCode() == 200)
     }
     test("failure") - withServer(MinimalApplication) { host =>
       val response1 = requests.post(host, data = ujson.Obj("name" -> "haoyi"), check = false)
diff --git a/14.4 - Websockets/build.sc b/14.7 - WebsocketsJsoup/build.sc
index 6f2d488..baac09f 100644
--- a/14.4 - Websockets/build.sc	
+++ b/14.7 - WebsocketsJsoup/build.sc	
@@ -11,7 +11,8 @@ object app extends ScalaModule {
 
     def ivyDeps = Agg(
       ivy"com.lihaoyi::utest:0.7.4",
-      ivy"com.lihaoyi::requests:0.6.2"
+      ivy"com.lihaoyi::requests:0.6.2",
+      ivy"org.jsoup:jsoup:1.12.1"
     )
   }
 }
```