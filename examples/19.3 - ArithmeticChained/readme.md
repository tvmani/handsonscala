```bash
amm TestArithmetic.sc
```

Diff from [19.2 - Arithmetic](https://github.com/handsonscala/handsonscala/tree/master/examples/19.19.2%20-%20Arithmetic):
```diff
diff --git a/19.2 - Arithmetic/Arithmetic.sc b/19.3 - ArithmeticChained/Arithmetic.sc
index 599cdc7..4fed504 100644
--- a/19.2 - Arithmetic/Arithmetic.sc	
+++ b/19.3 - ArithmeticChained/Arithmetic.sc	
@@ -20,6 +20,8 @@ def operator[_: P] = P( "plus" | "minus" | "times" | "divide" ).!
 
 def expr[_: P] = P( "(" ~ parser ~ ")" | number )
 
-def parser[_: P]: P[Expr] = P( expr ~ ws ~ operator ~ ws ~ expr ).map{
-  case (lhs, op, rhs) => BinOp(lhs, op, rhs)
+def parser[_: P]: P[Expr] = P( expr ~ (ws ~ operator ~ ws ~ expr).rep ).map{
+  case (lhs, rights) => rights.foldLeft(lhs){
+    case (left, (op, right)) => BinOp(left, op, right)
+  }
 }
diff --git a/19.2 - Arithmetic/TestArithmetic.sc b/19.3 - ArithmeticChained/TestArithmetic.sc
index 04c22b8..62b2da5 100644
--- a/19.2 - Arithmetic/TestArithmetic.sc	
+++ b/19.3 - ArithmeticChained/TestArithmetic.sc	
@@ -17,7 +17,9 @@ def evaluate(e: Expr): Int = e match {
   case Number(n) => n
 }
 
-val t = fastparse.parse("(one plus two) times (three plus four)", parser(_)).get.value
+val t = fastparse.parse("one plus two times three plus four", parser(_)).get.value
 
-assert(stringify(t) == "((one plus two) times (three plus four))")
-assert(evaluate(t) == 21)
+pprint.log(stringify(t))
+assert(stringify(t) == "(((one plus two) times three) plus four)")
+pprint.log(evaluate(t))
+assert(evaluate(t) == 13)
```