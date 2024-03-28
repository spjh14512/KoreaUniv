package kuplrg

object Implementation extends Template {

  import Expr.*

  def interp(expr: Expr, env: Env): Value = expr match
    // numbers
    case Num(number: BigInt) => number
    // additions
    case Add(left: Expr, right: Expr) => interp(left, env) + interp(right, env)
    // multiplications
    case Mul(left: Expr, right: Expr) => interp(left, env) * interp(right, env)
    // immutable variable definitions
    case Val(name: String, init: Expr, body: Expr) => interp(body, env + (name -> interp(init, env)))
    // identifier lookups
    case Id(name: String) => env.getOrElse(name, error("free identifier"))

  def freeIdsWithEnvKeys(expr: Expr, envKeys: Set[String]): Set[String] = expr match
    // numbers
    case Num(number: BigInt)
    => Set()
    // additions
    case Add(left: Expr, right: Expr)
    => freeIdsWithEnvKeys(left, envKeys) ++ freeIdsWithEnvKeys(right, envKeys)
    // multiplications
    case Mul(left: Expr, right: Expr)
    => freeIdsWithEnvKeys(left, envKeys) ++ freeIdsWithEnvKeys(right, envKeys)
    // immutable variable definitions
    case Val(name: String, init: Expr, body: Expr)
    => freeIdsWithEnvKeys(init, envKeys) ++ freeIdsWithEnvKeys(body, envKeys + name)
    // identifier lookups
    case Id(name: String)
    => if (envKeys.contains(name)) Set() else Set(name)

  def freeIds(expr: Expr): Set[String] = expr match
    // numbers
    case Num(number: BigInt)
    => Set()
    // additions
    case Add(left: Expr, right: Expr)
    => freeIds(left) ++ freeIds(right)
    // multiplications
    case Mul(left: Expr, right: Expr)
    => freeIds(left) ++ freeIds(right)
    // immutable variable definitions
    case Val(name: String, init: Expr, body: Expr)
    => freeIdsWithEnvKeys(init, Set()) ++ freeIdsWithEnvKeys(body, Set(name))
    // identifier lookups
    case Id(name: String) => Set(name)

  def bindingIds(expr: Expr): Set[String] = expr match
    case Val(name, init, body) => Set(name) ++ bindingIds(init) ++ bindingIds(body)
    case _ => Set()

  def boundIds(expr: Expr): Set[String] = ???

  def shadowedIds(expr: Expr): Set[String] = ???

}
