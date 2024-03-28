package kuplrg

object Implementation extends Template {

  import Expr.*
  import Value.*
  import Cont.*

  def interpCPS(expr: Expr, env: Env, k: Value => Value): Value = expr match
    // numbers
    case Num(number: BigInt) => k(NumV(number))
    // additions
    case Add(left: Expr, right: Expr) => interpCPS(
        left, env, lv => interpCPS(
          right, env, rv => k(numAdd(lv, rv))
        )
      )
    // multiplications
    case Mul(left: Expr, right: Expr) => interpCPS(
        left, env, lv => interpCPS(
          right, env, rv => k(numMul(lv, rv))
        )
      )
    // identifier lookups
    case Id(name: String) => k(env.getOrElse(name, error("free identifier")))
    // anonymous (lambda) functions
    case Fun(param: String, body: Expr) => k(CloV(param, body, env))
    // function applications
    case App(fun: Expr, arg: Expr) => interpCPS(
        fun, env, fv => interpCPS(
          arg, env, argv => k(funApp(fv, argv))
        )
      )

  def numAdd(lv: Value, rv: Value): Value = (lv, rv) match
    case (NumV(l), NumV(r)) => NumV(l + r)
    case _ => error("invalid operation")

  def numMul(lv: Value, rv: Value): Value = (lv, rv) match
    case (NumV(l), NumV(r)) => NumV(l * r)
    case _ => error("invalid operation")

  def funApp(fv: Value, argv: Value): Value = (fv, argv) match
    case (CloV(p, b, fenv), argv) => interpCPS(b, fenv + (p -> argv), x => x)
    case _ => error("not a function")

  def reduce(k: Cont, s: Stack): (Cont, Stack) = (k, s) match
    case (EmptyK, s) => (EmptyK, s)
    case (EvalK(env, expr, k), s) => expr match
      case Num(number: BigInt) => (k, NumV(number) :: s)
      case Add(l, r) => (EvalK(env, l, EvalK(env, r, AddK(k))), s)
      case Mul(l, r) => (EvalK(env, l, EvalK(env, r, MulK(k))), s)
      case Id(name) => (k, env.getOrElse(name, error("free identifier")) :: s)
      case Fun(param, body) => (k, CloV(param, body, env) :: s)
      case App(fun, arg) => (EvalK(env, fun, EvalK(env, arg, AppK(k))), s)
    case (AddK(k), rv :: lv :: s) => (k, numAdd(lv, rv) :: s)
    case (MulK(k), rv :: lv :: s) => (k, numMul(lv, rv) :: s)
    case (AppK(k), argv :: funv :: s) => (k, funApp(funv, argv) :: s)

}
