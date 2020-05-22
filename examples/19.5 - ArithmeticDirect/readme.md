```bash
amm TestArithmetic.sc
```


Diff from [19.2 - Arithmetic](https://github.com/handsonscala/handsonscala/tree/master/examples/19.19.2%20-%20Arithmetic):
```diff
diff --git a/19.2 - Arithmetic/Arithmetic.sc b/19.5 - ArithmeticDirect/Arithmetic.sc
index 599cdc7..9726ee3 100644
--- a/19.2 - Arithmetic/Arithmetic.sc	
+++ b/19.5 - ArithmeticDirect/Arithmetic.sc	
@@ -1,17 +1,12 @@
 import fastparse._, NoWhitespace._
 
-sealed trait Expr
-case class BinOp(left: Expr, op: String, right: Expr) extends Expr
-case class Number(Value: Int) extends Expr
 def number[_: P] = P(
   "zero" | "one" | "two" | "three" | "four" |
     "five" | "six" | "seven" | "eight" | "nine"
 ).!.map{
-  case "zero"  => Number(0); case "one"   => Number(1)
-  case "two"   => Number(2); case "three" => Number(3)
-  case "four"  => Number(4); case "five"  => Number(5)
-  case "six"   => Number(6); case "seven" => Number(7)
-  case "eight" => Number(8); case "nine"  => Number(9)
+  case "zero"  => 0; case "one"   => 1; case "two" => 2; case "three" => 3
+  case "four"  => 4; case "five"  => 5; case "six" => 6; case "seven" => 7
+  case "eight" => 8; case "nine"  => 9
 }
 
 def ws[_: P] = P( " ".rep(1) )
@@ -20,6 +15,9 @@ def operator[_: P] = P( "plus" | "minus" | "times" | "divide" ).!
 
 def expr[_: P] = P( "(" ~ parser ~ ")" | number )
 
-def parser[_: P]: P[Expr] = P( expr ~ ws ~ operator ~ ws ~ expr ).map{
-  case (lhs, op, rhs) => BinOp(lhs, op, rhs)
+def parser[_: P]: P[Int] = P( expr ~ ws ~ operator ~ ws ~ expr ).map{
+  case (lhs, "plus", rhs) => lhs + rhs
+  case (lhs, "minus", rhs) => lhs - rhs
+  case (lhs, "times", rhs) => lhs * rhs
+  case (lhs, "divide", rhs) => lhs / rhs
 }
diff --git a/19.2 - Arithmetic/TestArithmetic.sc b/19.5 - ArithmeticDirect/TestArithmetic.sc
index 04c22b8..ada9cef 100644
--- a/19.2 - Arithmetic/TestArithmetic.sc	
+++ b/19.5 - ArithmeticDirect/TestArithmetic.sc	
@@ -1,23 +1,15 @@
 import $file.Arithmetic, Arithmetic._
-
-def stringify(e: Expr): String = e match {
-  case BinOp(left, op, right) => s"(${stringify(left)} $op ${stringify(right)})"
-  case Number(0) => "zero"; case Number(1) => "one"
-  case Number(2) => "two"; case Number(3) => "three"
-  case Number(4) => "four"; case Number(5) => "five"
-  case Number(6) => "six"; case Number(7) => "seven"
-  case Number(8) => "eight"; case Number(9) => "seven"
-}
-
-def evaluate(e: Expr): Int = e match {
-  case BinOp(left, "plus", right) => evaluate(left) + evaluate(right)
-  case BinOp(left, "minus", right) => evaluate(left) - evaluate(right)
-  case BinOp(left, "times", right) => evaluate(left) * evaluate(right)
-  case BinOp(left, "divide", right) => evaluate(left) / evaluate(right)
-  case Number(n) => n
-}
-
-val t = fastparse.parse("(one plus two) times (three plus four)", parser(_)).get.value
-
-assert(stringify(t) == "((one plus two) times (three plus four))")
-assert(evaluate(t) == 21)
+import fastparse._
+
+assert(
+  pprint.log(fastparse.parse("three times seven", parser(_))) ==
+    Parsed.Success(value = 21, index = 17)
+)
+assert(
+  pprint.log(fastparse.parse("(eight divide two) times (nine minus four)", parser(_))) ==
+    Parsed.Success(value = 20, index = 42)
+)
+assert(
+  pprint.log(fastparse.parse("five times ((nine times eight) minus four)", parser(_))) ==
+    Parsed.Success(value = 340, index = 42)
+)
```