```bash
./mill -i app.test
```


Diff from [14.1 - Mock](https://github.com/handsonscala/handsonscala/tree/master/examples/14.14.1%20-%20Mock):
```diff
diff --git a/14.1 - Mock/app/src/MinimalApplication.scala b/14.2 - Forms/app/src/MinimalApplication.scala
index 582566c..a90d3c5 100644
--- a/14.1 - Mock/app/src/MinimalApplication.scala	
+++ b/14.2 - Forms/app/src/MinimalApplication.scala	
@@ -1,8 +1,11 @@
 package app
 import scalatags.Text.all._
 object MinimalApplication extends cask.MainRoutes {
+  var messages = Vector(("alice", "Hello World!"), ("bob", "I am cow, hear me moo"))
   @cask.get("/")
-  def hello() = doctype("html")(
+  def hello(errorOpt: Option[String] = None,
+            userName: Option[String] = None,
+            msg: Option[String] = None) = doctype("html")(
     html(
       head(
         link(
@@ -13,18 +16,37 @@ object MinimalApplication extends cask.MainRoutes {
       body(
         div(cls := "container")(
           h1("Scala Chat!"),
-          div(
-            p(b("alice"), " ", "Hello World!"),
-            p(b("bob"), " ", "I am cow, hear me moo"),
+          div(for ((name, msg) <- messages) yield p(b(name), " ", msg)),
+          for (error <- errorOpt) yield i(color.red)(error),
+          form(action := "/", method := "post")(
+            input(
+              `type` := "text",
+              name := "name",
+              placeholder := "User name",
+              userName.map(value := _)
             ),
-          div(
-            input(`type` := "text", placeholder := "User name"),
-            input(`type` := "text", placeholder := "Write a message!")
+            input(
+              `type` := "text",
+              name := "msg",
+              placeholder := "Write a message!",
+              msg.map(value := _)
+            ),
+            input(`type` := "submit")
           )
         )
       )
     )
   )
 
+  @cask.postForm("/")
+  def postHello(name: String, msg: String) = {
+    if (name == "") hello(Some("Name cannot be empty"), Some(name), Some(msg))
+    else if (msg == "") hello(Some("Message cannot be empty"), Some(name), Some(msg))
+    else {
+      messages = messages :+ (name -> msg)
+      hello(None, Some(name), None)
+    }
+  }
+
   initialize()
 }
diff --git a/14.1 - Mock/app/test/src/ExampleTests.scala b/14.2 - Forms/app/test/src/ExampleTests.scala
index 7280936..feb6e49 100644
--- a/14.1 - Mock/app/test/src/ExampleTests.scala	
+++ b/14.2 - Forms/app/test/src/ExampleTests.scala	
@@ -5,12 +5,12 @@ import utest._
 object ExampleTests extends TestSuite {
   def withServer[T](example: cask.main.Main)(f: String => T): T = {
     val server = io.undertow.Undertow.builder
-      .addHttpListener(8081, "localhost")
+      .addHttpListener(8082, "localhost")
       .setHandler(example.defaultHandler)
       .build
     server.start()
     val res =
-      try f("http://localhost:8081")
+      try f("http://localhost:8082")
       finally server.stop()
     res
   }
@@ -25,6 +25,25 @@ object ExampleTests extends TestSuite {
       assert(success.text().contains("bob"))
       assert(success.text().contains("I am cow, hear me moo"))
       assert(success.statusCode == 200)
+
+      val response = requests.post(host, data = Map("name" -> "haoyi", "msg" -> "Test Message!"))
+
+      assert(response.text().contains("Scala Chat!"))
+      assert(response.text().contains("alice"))
+      assert(response.text().contains("Hello World!"))
+      assert(response.text().contains("bob"))
+      assert(response.text().contains("I am cow, hear me moo"))
+      assert(response.text().contains("haoyi"))
+      assert(response.text().contains("Test Message!"))
+      assert(response.statusCode == 200)
+    }
+    test("failure") - withServer(MinimalApplication) { host =>
+      val response1 = requests.post(host, data = Map("name" -> "haoyi"), check = false)
+      assert(response1.statusCode == 400)
+      val response2 = requests.post(host, data = Map("name" -> "haoyi", "msg" -> ""))
+      assert(response2.text().contains("Message cannot be empty"))
+      val response3 = requests.post(host, data = Map("name" -> "", "msg" -> "Test Message!"))
+      assert(response3.text().contains("Name cannot be empty"))
     }
   }
 }
```