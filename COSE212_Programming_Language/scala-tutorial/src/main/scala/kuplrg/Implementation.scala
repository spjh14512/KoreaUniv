package kuplrg

object Implementation extends Template {

  def sqsum(x: Int, y: Int): Int = x * x + y * y

  def concat(left: String, right: String): String = left + right

  def subN(n: Int): Int => Int = (x) => (x - n)

  def twice(f: Int => Int): Int => Int = (x) => f(f(x))

  def compose(f: Int => Int, g: Int => Int): Int => Int = (x) => f(g(x))

  def sumOnlyOdd(l: List[Int]): Int = l match
    case Nil => 0
    case x :: xs if x % 2 == 1 => x + sumOnlyOdd(xs)
    case x :: xs => sumOnlyOdd(xs)

  def foldWith(f: (Int, Int) => Int): List[Int] => Int = (l) => l.foldLeft(0)(f)

  def toSet(l: List[Int], from: Int): Set[Int] = l.drop(from).toSet

  def getOrZero(map: Map[String, Int], key: String): Int = map.getOrElse(key, 0)

  def setMinus(s1: Set[Int], s2: Set[Int]): Set[Int] = s1.diff(s2)

  // ---------------------------------------------------------------------------
  // Trees
  // ---------------------------------------------------------------------------
  import Tree.*

  def has(value: Int): Tree => Boolean = (tree) => tree match
    case Leaf(v) => if v == value then true else false
    case Branch(l, v, r) => if v == value || has(value)(l) || has(value)(r) then true else false

  def maxDepthOf(value: Int): Tree => Option[Int] = (tree) => tree match
  case Leaf(v) if v == value => Some(0)
  case Branch(l, v, r) if v == value => Some(0)
  case Leaf(v) => None
  case Branch(l, v, r) =>
    val leftDepth = maxDepthOf(value)(l)
    val rightDepth = maxDepthOf(value)(r)
    (leftDepth, rightDepth) match
    case (Some(ld), Some(rd)) => if ld > rd then Some(1 + ld) else Some(1 + rd)
    case (Some(ld), None) => Some(1 + ld)
    case (None, Some(rd)) => Some(1 + rd)
    case (None, None) => None

  def mul(t: Tree): Int = t match
    case Leaf(v) => v
    case Branch(l, v, r) => mul(l) * v * mul(r)

  def countLeaves(t: Tree): Int = t match
    case Leaf(v) => 1
    case Branch(l, v, r) => countLeaves(l) + countLeaves(r)

  def postOrder(t: Tree): List[Int] = t match
    case Leaf(v) => List(v)
    case Branch(l, v, r) => postOrder(l) ++ postOrder(r) ++ List(v)

  // ---------------------------------------------------------------------------
  // Boolean Expressions
  // ---------------------------------------------------------------------------
  import BE.*

  def countLiterals(expr: BE): Int = expr match
    case True => 1
    case False => 1
    case And(left: BE, right: BE) => countLiterals(left) + countLiterals(right)
    case Or(left: BE, right: BE) => countLiterals(left) + countLiterals(right)
    case Not(exp: BE) => countLiterals(exp)

  def countNots(expr: BE): Int = expr match
    case True => 0
    case False => 0
    case And(left: BE, right: BE) => countNots(left) + countNots(right)
    case Or(left: BE, right: BE) => countNots(left) + countNots(right)
    case Not(exp: BE) => 1 + countNots(exp)

  def depth(expr: BE): Int = expr match
    case True => 0
    case False => 0
    case And(left: BE, right: BE) => if depth(left) > depth(right) then 1 + depth(left) else 1 + depth(right)
    case Or(left: BE, right: BE) => if depth(left) > depth(right) then 1 + depth(left) else 1 + depth(right)
    case Not(exp: BE) => 1 + depth(exp)

  def getString(expr: BE): String = expr match
    case True => "true"
    case False => "false"
    case And(left: BE, right: BE) => "(" + getString(left) + " & " + getString(right) + ")"
    case Or(left: BE, right: BE) => "(" + getString(left) + " | " + getString(right) + ")"
    case Not(exp: BE) => "!" + getString(exp)

  def eval(expr: BE): Boolean = expr match
    case True => true
    case False => false
    case And(left: BE, right: BE) => eval(left) && eval(right)
    case Or(left: BE, right: BE) => eval(left) || eval(right)
    case Not(exp: BE) => !eval(exp)
}
