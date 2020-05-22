```bash
amm TestArithmetic.sc
```

Diff from [19.3 - ArithmeticChained](https://github.com/handsonscala/handsonscala/tree/master/examples/19.19.3%20-%20ArithmeticChained):
```diff
diff --git a/19.3 - ArithmeticChained/Arithmetic.sc b/19.4 - ArithmeticPrecedence/Arithmetic.sc
index 4fed504..9e43124 100644
--- a/19.3 - ArithmeticChained/Arithmetic.sc	
+++ b/19.4 - ArithmeticPrecedence/Arithmetic.sc	
@@ -20,8 +20,29 @@ def operator[_: P] = P( "plus" | "minus" | "times" | "divide" ).!
 
 def expr[_: P] = P( "(" ~ parser ~ ")" | number )
 
-def parser[_: P]: P[Expr] = P( expr ~ (ws ~ operator ~ ws ~ expr).rep ).map{
-  case (lhs, rights) => rights.foldLeft(lhs){
-    case (left, (op, right)) => BinOp(left, op, right)
+val precedence = Map("plus" -> 1, "minus" -> 1, "times" -> 2, "divide" -> 2)
+
+def climb(left: Expr,
+          rights: collection.mutable.ArrayDeque[(String, Expr)],
+          minPrec: Int): Expr = {
+  var result = left
+  var done = false
+  while(!done) {
+    rights.headOption match {
+      case None => done = true
+      case Some((op, next)) =>
+        val prec: Int = precedence(op)
+        if (prec < minPrec) done = true
+        else{
+          rights.removeHead()
+          val rhs = climb(next, rights, prec + 1)
+          result = BinOp(result, op, rhs)
+        }
+    }
   }
+  result
+}
+
+def parser[_: P]: P[Expr] = P( expr ~ (ws ~ operator ~ ws ~ expr).rep ).map{
+  case (lhs, rights) => climb(lhs, rights.to(collection.mutable.ArrayDeque), 1)
 }
diff --git a/19.3 - ArithmeticChained/TestArithmetic.sc b/19.4 - ArithmeticPrecedence/TestArithmetic.sc
index 62b2da5..60ba8c5 100644
--- a/19.3 - ArithmeticChained/TestArithmetic.sc	
+++ b/19.4 - ArithmeticPrecedence/TestArithmetic.sc	
@@ -20,6 +20,6 @@ def evaluate(e: Expr): Int = e match {
 val t = fastparse.parse("one plus two times three plus four", parser(_)).get.value
 
 pprint.log(stringify(t))
-assert(stringify(t) == "(((one plus two) times three) plus four)")
+assert(stringify(t) == "((one plus (two times three)) plus four)")
 pprint.log(evaluate(t))
-assert(evaluate(t) == 13)
+assert(evaluate(t) == 11)
```