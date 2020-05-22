-   def hello() = {
-     "Hello World!"
-   }
+   def hello() = doctype("html")(
+     html(
+       head(
+         link(
+           rel := "stylesheet",
+           href := "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"
+         )
+       ),
+       body(
+         div(cls := "container")(
+           h1("Hello!"),
+           p("World")
+         )
+       )
+     )
+   )