import fastparse._, NoWhitespace._

sealed trait Expr
case class BinOp(left: Expr, op: String, right: Expr) extends Expr
case class Number(Value: Int) extends Expr

def number[_: P] = P(
  "zero" | "one" | "two" | "three" | "four" |
  "five" | "six" | "seven" | "eight" | "nine"
).!.map{
  case "zero"  => Number(0); case "one"   => Number(1)
  case "two"   => Number(2); case "three" => Number(3)
  case "four"  => Number(4); case "five"  => Number(5)
  case "six"   => Number(6); case "seven" => Number(7)
  case "eight" => Number(8); case "nine"  => Number(9)
}

def ws[_: P] = P( " ".rep(1) )

def operator[_: P] = P( "plus" | "minus" | "times" | "divide" ).!

def expr[_: P] = P( "(" ~ parser ~ ")" | number )

def parser[_: P]: P[Expr] = P( expr ~ ws ~ operator ~ ws ~ expr ).map{
  case (lhs, op, rhs) => BinOp(lhs, op, rhs)
}

def stringify(e: Expr): String = e match {
  case BinOp(left, op, right) => s"(${stringify(left)} $op ${stringify(right)})"
  case Number(0) => "zero"; case Number(1) => "one"
  case Number(2) => "two"; case Number(3) => "three"
  case Number(4) => "four"; case Number(5) => "five"
  case Number(6) => "six"; case Number(7) => "seven"
  case Number(8) => "eight"; case Number(9) => "seven"
}

def evaluate(e: Expr): Int = e match {
  case BinOp(left, "plus", right) => evaluate(left) + evaluate(right)
  case BinOp(left, "minus", right) => evaluate(left) - evaluate(right)
  case BinOp(left, "times", right) => evaluate(left) * evaluate(right)
  case BinOp(left, "divide", right) => evaluate(left) / evaluate(right)
  case Number(n) => n
}