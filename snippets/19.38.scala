-def operator[_: P] = P( "plus" | "minus" | "times" | "divide" ).!
+def operator[_: P] = P( "plus" | "minus" | "times" | "divide" ).!.log

-def expr[_: P] = P( "(" ~ parser ~ ")" | number )
+def expr[_: P] = P( "(" ~ parser ~ ")" | number ).log

 def parser[_: P]: P[Expr] = P( expr ~ ws ~ operator ~ ws ~ expr ).map{
   case (lhs, op, rhs) => BinOp(lhs, op, rhs)
-}
+}.log