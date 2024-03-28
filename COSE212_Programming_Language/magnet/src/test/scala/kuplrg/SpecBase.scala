package kuplrg

import java.io.{File, PrintWriter}
import java.util.concurrent.TimeoutException
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import scala.Console.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.io.Source
import scala.quoted.*
import scala.util.Try

trait SpecBase extends AnyFlatSpec with BeforeAndAfterAll {
  import SpecBase.*

  try Implementation catch
    case err: Throwable =>
      printlnColor(RED)(unexpectedErrorMsg(err))
      throw err

  val DEFAULT_TIME_LIMIT = 5
  val TIME_LIMIT = Try(readFile("time_limit").trim.toInt)
    .getOrElse(DEFAULT_TIME_LIMIT)
  println(s"[info] time limit: $TIME_LIMIT sec")

  def doCheck[T](
    exprCode: String,
    result: => T,
    weight: Int = 1,
  ): Unit = newTest(exprCode, weight = weight) {
    result
    None
  }

  inline def check[T](
    inline expr: T,
    inline weight: Int = 1,
  ): Unit =
    ${ checkImpl('expr, 'doCheck, 'weight) }

  def doTest[T](
    exprCode: String,
    answerCode: String,
    result: => T,
    expected: => T,
    weight: Int = 1,
  ): Unit = newTest(exprCode, s"be $answerCode", weight = weight) {
    if (result != expected) Some(s"$result != $expected")
    else None
  }

  inline def test[T](
    inline expr: T,
    inline answer: T,
    inline weight: Int = 1,
  ): Unit =
    ${ testImpl('expr, 'answer, 'doTest, 'weight) }

  def doTestExc[T](
    exprCode: String,
    result: => T,
    errMsg: => String = "",
    weight: Int = 1,
  ): Unit = newTest(
    exprCode,
    if (errMsg.isEmpty) "throw an error"
    else s"throw an error with a message \"$errMsg\"",
    weight
  ) {
    try
      Some(s"""It must throw an error but returns $result without errors""")
    catch
      case err: PLError =>
        val msg = err.msg
        if (!err.msg.contains(errMsg))
          Some(s"""Error message should contain "$errMsg" but got "$msg"""")
        else None
  }

  inline def testExc[T](
    inline expr: T,
    inline errMsg: String = "",
    inline weight: Int = 1,
  ): Unit =
    ${ testExcImpl('expr, 'errMsg, 'doTestExc, 'weight) }

  def afterTest: Unit = ()

  def passAll: Boolean = getFailCount == 0

  def getFailCount: Int = failCount

  private def newTest[T](
    exprCode: String,
    cond: String,
    weight: Int,
  )(result: => Option[String]): Unit =
    newTest(exprCode, Some(cond), weight)(result)
  private def newTest[T](
    exprCode: String,
    cond: Option[String] = None,
    weight: Int = 1,
  )(result: => Option[String]): Unit =
    exprCode should cond.getOrElse("be terminated normally") in {
      val weightMsg =
        if (weight == 1) ""
        else setColor(CYAN)(s" [$weight pts]")
      info(exprCode + cond.fold("")(cond => s" should $cond") + weightMsg)
      val msg = try
        Await.result(Future(Try(result)), TIME_LIMIT.second).get
      catch
        case err: NotImplementedError => failed(notImpl(err), weight)
        case _: TimeoutException => failed("Timeout", weight)
        case err: Throwable => failed(err, weight)
      msg.fold(passed(weight))(failed(_, weight))
    }

  private def info(msg: String): Unit =
    printColor(CYAN)(s"[test] ")
    print(msg)

  private def failed(err: Throwable, weight: Int): Nothing =
    val msg = err match
      case err: PLError => err.msg
      case _ => unexpectedErrorMsg(err)
    failed(s"[ERROR] $msg", weight)
  private def failed(msg: String, weight: Int): Nothing =
    printlnColor(RED)(" - FAIL")
    printlnColor(RED)(s"       $msg")
    failCount += weight
    fail(msg)
  private def notImpl(err: NotImplementedError): String =
    val elem = err.getStackTrace.apply(1)
    val line = elem.getLineNumber
    val methodName = elem.getMethodName
    s"Please implement `${norm(methodName)}` (Implementaion.scala:$line)."

  private def unexpectedErrorMsg(err: Throwable): String = (
    s"Unexpected Error - $err\n" +:
    err.getStackTrace.take(10).toVector
    :+ "..."
  ).mkString("\n       ")

  private def passed(weight: Int): Unit =
    printlnColor(GREEN)(" - PASS")
    passCount += weight

  private var failCount = 0
  private var passCount = 0
  override def afterAll(): Unit =
    val score = passCount * 100 / (passCount + failCount)
    println("----------------------------------------")
    println(s"[SCORE] $score ($passCount / ${passCount + failCount})")
    println("----------------------------------------")
    dumpFile(score, "score")
    try afterTest catch
      case err: Throwable => printlnColor(RED)("[Error] " + (err match
        case err: NotImplementedError => notImpl(err)
        case _ => unexpectedErrorMsg(err)
      ))

  def mkdir(name: String): Unit = File(name).mkdirs

  def getPrintWriter(filename: String): PrintWriter =
    val file = File(filename)
    val parent = file.getParent
    if (parent != null) mkdir(parent)
    PrintWriter(file)

  def readFile(filename: String): String =
    val source = Source.fromFile(filename, "utf8")
    val str = source.mkString
    source.close
    str

  def dumpFile(data: Any, filename: String): Unit =
    val nf = getPrintWriter(filename)
    nf.print(data)
    nf.close()
}

private object SpecBase:
  def checkImpl[T](
    expr: Expr[T],
    doit: Expr[(String, => T, Int) => Unit],
    weight: Expr[Int],
  )(using Type[T], Quotes): Expr[Unit] =
    val exprCode = normCode(expr)
    '{ $doit($exprCode, $expr, $weight) }

  def testImpl[T](
    expr: Expr[T],
    answer: Expr[T],
    doit: Expr[(String, String, => T, => T, Int) => Unit],
    weight: Expr[Int],
  )(using Type[T], Quotes): Expr[Unit] =
    val exprCode = normCode(expr)
    val answerCode = normCode(answer)
    '{ $doit($exprCode, $answerCode, $expr, $answer, $weight) }

  def testExcImpl[T](
    expr: Expr[T],
    errMsg: Expr[String],
    doit: Expr[(String, => T, => String, Int) => Unit],
    weight: Expr[Int],
  )(using Type[T], Quotes): Expr[Unit] = '{
    $doit(${ normCode(expr) }, $expr, $errMsg, $weight)
  }

  val len = 70

  def normCode[T](expr: Expr[T])(using Quotes): Expr[String] =
    Expr(norm(expr.show))

  def norm(expr: Any): String =
    val str = expr.toString
      .replaceAll("this.", "")
      .replaceAll("kuplrg.", "")
      .replaceAll("scala\\.", "")
      .replaceAll("Spec\\.", "")
      .replaceAll("Implementation\\.", "")
      .replaceAll("\\.apply", "")
      .replaceAll("\\w+\\$package\\.", "")
      .replaceAll("\\$\\w*", "")
    if (str.length <= len) str
    else s"${str.substring(0, len)} ..."

  /** show colored message */
  def setColor(color: String): Any => String =
    if (color == "") x => x.toString else x => color + x.toString + RESET
  def printColor(color: String): Any => Unit = x => print(setColor(color)(x))
  def printlnColor(color: String): Any => Unit = x =>
    println(setColor(color)(x))
