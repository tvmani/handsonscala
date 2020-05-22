```bash
amm TestJsonnet.sc
```

Diff from [20.2 - JsonnetNumbers](https://github.com/handsonscala/handsonscala/tree/master/examples/20.20.2%20-%20JsonnetNumbers):
```diff
diff --git a/20.2 - JsonnetNumbers/Jsonnet.sc b/20.3 - JsonnetPrettyPrinting/Jsonnet.sc
index a66a3a0..40fe278 100644
--- a/20.2 - JsonnetNumbers/Jsonnet.sc	
+++ b/20.3 - JsonnetPrettyPrinting/Jsonnet.sc	
@@ -64,13 +64,15 @@ def evaluate(expr: Expr, scope: Map[String, Value]): Value = expr match {
     Value.Func(args => evaluate(body, scope ++ argNames.zip(args)))
 }
 
-def serialize(v: Value): String = v match {
-  case Value.Str(s) => "\"" + s + "\""
-  case Value.Num(i) => i.toString
-  case Value.Dict(kvs) =>
-    kvs.map{case (k, v) => "\"" + k + "\": " + serialize(v)}.mkString("{", ", ", "}")
+def serialize(v: Value): ujson.Value = v match {
+  case Value.Str(s) => ujson.Str(s)
+  case Value.Num(i) => ujson.Num(i)
+  case Value.Dict(kvs) => ujson.Obj.from(kvs.map{case (k, v) => (k, serialize(v))})
 }
 
 def jsonnet(input: String): String = {
-  serialize(evaluate(fastparse.parse(input, Parser.expr(_)).get.value, Map.empty))
+  ujson.write(
+    serialize(evaluate(fastparse.parse(input, Parser.expr(_)).get.value, Map.empty)),
+    indent = 2
+  )
 }
diff --git a/20.2 - JsonnetNumbers/TestJsonnet.sc b/20.3 - JsonnetPrettyPrinting/TestJsonnet.sc
index 4b5e005..238385d 100644
--- a/20.2 - JsonnetNumbers/TestJsonnet.sc	
+++ b/20.3 - JsonnetPrettyPrinting/TestJsonnet.sc	
@@ -11,9 +11,19 @@ assert(
        };
        {
          "person1": person("Alice", 50000),
-         "person2": person("Bob", 60000),
-         "person3": person("Charlie", 70000)
+         "person2": person("Bob", 60000)
        }"""
   )) ==
-  """{"person1": {"name": "Alice", "welcome": "Hello Alice!", "totalSalary": 65000}, "person2": {"name": "Bob", "welcome": "Hello Bob!", "totalSalary": 75000}, "person3": {"name": "Charlie", "welcome": "Hello Charlie!", "totalSalary": 85000}}"""
+  """{
+    |  "person1": {
+    |    "name": "Alice",
+    |    "welcome": "Hello Alice!",
+    |    "totalSalary": 65000
+    |  },
+    |  "person2": {
+    |    "name": "Bob",
+    |    "welcome": "Hello Bob!",
+    |    "totalSalary": 75000
+    |  }
+    |}""".stripMargin
 )
```