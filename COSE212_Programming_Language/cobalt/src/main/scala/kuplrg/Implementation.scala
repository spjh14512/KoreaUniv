package kuplrg

object Implementation extends Template {

  import Expr.*
  import Value.*

  def interp(expr: Expr, env: Env): Value = expr match
    // unit
    case EUnit => ()
    // numbers
    case ENum(number: BigInt) => NumV(number)
    // booleans
    case EBool(bool: Boolean) => BoolV(bool)
    // identifier lookups
    case EId(name: String) => env.getOrElse(name, error("free identifier"))
    // addition
    case EAdd(left: Expr, right: Expr) => (interp(left, env) + interp(right, env)) match
      case (NumV(l), NumV(r)) => NumV(l + r)
      case _ => error("invalid operation")
    // multiplication
    case EMul(left: Expr, right: Expr) => (interp(left, env) + interp(right, env)) match
      case (NumV(l), NumV(r)) => NumV(l * r)
      case _ => error("invalid operation")
    // division
    case EDiv(left: Expr, right: Expr) => (interp(left, env) + interp(right, env)) match
      case (NumV(l), NumV(r)) => NumV(l + r)
      case _ => error("invalid operation")
    // modulo
    case EMod(left: Expr, right: Expr)
    // equal-to
    case EEq(left: Expr, right: Expr)
    // less-than
    case ELt(left: Expr, right: Expr)
    // conditional
    case EIf(cond: Expr, thenExpr: Expr, elseExpr: Expr)
    // empty list
    case ENil
    // list cons
    case ECons(head: Expr, tail: Expr)
    // list head
    case EHead(list: Expr)
    // list tail
    case ETail(list: Expr)
    // list length
    case ELength(list: Expr)
    // list map function
    case EMap(list: Expr, fun: Expr)
    // list flatMap function
    case EFlatMap(list: Expr, fun: Expr)
    // list filter function
    case EFilter(list: Expr, fun: Expr)
    // tuple
    case ETuple(exprs: List[Expr])
    // tuple projection
    case EProj(tuple: Expr, index: Int)
    // variable definition
    case EVal(name: String, value: Expr, scope: Expr)
    // lambda function
    case EFun(params: List[String], body: Expr)
    // mutually recursive function
    case ERec(defs: List[FunDef], scope: Expr)
    // function application
    case EApp(fun: Expr, args: List[Expr])


  def eq(left: Value, right: Value): Boolean = ???

  def length(list: Value): BigInt = ???

  def map(list: Value, fun: Value): Value = ???

  def join(list: Value): Value = ???

  def filter(list: Value, fun: Value): Value = ???

  def app(fun: Value, args: List[Value]): Value = ???
}
