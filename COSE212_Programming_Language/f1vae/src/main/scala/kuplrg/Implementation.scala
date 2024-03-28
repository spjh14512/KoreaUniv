package kuplrg

object Implementation extends Template {

  import Expr.*

  def interp(expr: Expr, env: Env, fenv: FEnv): Value = expr match
    // numbers
    case Num(number: BigInt) => number
    // additions
    case Add(left: Expr, right: Expr) => interp(left, env, fenv) + interp(right, env, fenv)
    // multiplications
    case Mul(left: Expr, right: Expr) => interp(left, env, fenv) * interp(right, env, fenv)
    // immutable variable definitions
    case Val(name: String, init: Expr, body: Expr)
      => interp(body, env + (name -> interp(init, env, fenv)), fenv)
    // identifier lookups
    case Id(name: String) => env.getOrElse(name, error("free identifier"))
    // function applications
    case App(fname: String, arg: Expr) =>
      fenv.get(fname) match
        case Some(FunDef(name, param, body)) => interp(body, Map(param -> interp(arg, env, fenv)), fenv)
        case None => error("unknown function")


  def interpDS(expr: Expr, env: Env, fenv: FEnv): Value = expr match
    // numbers
    case Num(number: BigInt)
    => number
    // additions
    case Add(left: Expr, right: Expr)
    => interp(left, env, fenv) + interp(right, env, fenv)
    // multiplications
    case Mul(left: Expr, right: Expr)
    => interp(left, env, fenv) * interp(right, env, fenv)
    // immutable variable definitions
    case Val(name: String, init: Expr, body: Expr)
    => interp(body, env + (name -> interp(init, env, fenv)), fenv)
    // identifier lookups
    case Id(name: String)
    => env.getOrElse(name, error("free identifier"))
    // function applications
    case App(fname: String, arg: Expr)
    => fenv.get(fname) match
      case Some(FunDef(name, param, body))
      => interp(body, env + (param -> interp(arg, env, fenv)), fenv)
      case None => error("unknown function")
}
