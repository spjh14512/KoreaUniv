package kuplrg

object Implementation extends Template {

  import Expr.*
  import RecDef.*
  import Value.*
  import Type.*
  import TypeInfo.*

  def typeCheck(expr: Expr, tenv: TypeEnv): Type = expr match
    // unit
    case EUnit => UnitT
    // numbers
    case ENum(number: BigInt) => NumT
    // booleans
    case EBool(bool: Boolean) => BoolT
    // strings
    case EStr(string: String) => StrT
    // identifier lookups
    case EId(name: String) => tenv.vars.getOrElse(name, error("1"))
    // addition
    case EAdd(left: Expr, right: Expr) => (typeCheck(left, tenv), typeCheck(right, tenv)) match
      case (NumT, NumT) => NumT
      case _ => error("2")
    // multiplication
    case EMul(left: Expr, right: Expr) => (typeCheck(left, tenv), typeCheck(right, tenv)) match
      case (NumT, NumT) => NumT
      case _ => error("3")
    // division
    case EDiv(left: Expr, right: Expr) => (typeCheck(left, tenv), typeCheck(right, tenv)) match
      case (NumT, NumT) => NumT
      case _ => error("4")
    // modulo
    case EMod(left: Expr, right: Expr) => (typeCheck(left, tenv), typeCheck(right, tenv)) match
      case (NumT, NumT) => NumT
      case _ => error("5")
    // string concatenation
    case EConcat(left: Expr, right: Expr) => (typeCheck(left, tenv), typeCheck(right, tenv)) match
      case (StrT, StrT) => StrT
      case _ => error("6")
    // equal-to
    case EEq(left: Expr, right: Expr) =>
      val ty1 = typeCheck(left, tenv)
      val ty2 = typeCheck(right, tenv)
      if (mustSame(ty1, ty2)) BoolT else error("7")
    // less-than
    case ELt(left: Expr, right: Expr) => (typeCheck(left, tenv), typeCheck(right, tenv)) match
      case (NumT, NumT) => BoolT
      case _ => error("8")
    // sequence
    case ESeq(left: Expr, right: Expr) =>
      val ty1 = typeCheck(left, tenv)
      typeCheck(right, tenv)
    // conditional
    case EIf(cond: Expr, thenExpr: Expr, elseExpr: Expr) =>
      val condTy = typeCheck(cond, tenv)
      val ty1 = typeCheck(thenExpr, tenv)
      val ty2 = typeCheck(elseExpr, tenv)
      if (mustSame(condTy, BoolT) && mustSame(ty1, ty2)) ty1 else error("9")
    // immutable variable definitions
    case EVal(x: String, tyOpt: Option[Type], expr: Expr, body: Expr) => tyOpt match
      case Some(ty0) =>
        if (mustSame(ty0, typeCheck(expr, tenv))) typeCheck(body, tenv.addVar(x, ty0)) else error()
      case None => typeCheck(body, tenv.addVar(x, typeCheck(expr, tenv)))
    // anonymous (lambda) functions
    case EFun(params: List[Param], body: Expr) =>
      if (params.map(p => mustValid(p.ty, tenv)).foldLeft(true)(_ && _))
        ArrowT(Nil, params.map(p => p.ty).toList, typeCheck(body, tenv.addVars(params.map(p => (p.name, p.ty)))))
      else error("10")
    // function applications
    case EApp(fun: Expr, tys: List[Type], args: List[Expr]) =>
      if (tys.map(t => mustValid(t, tenv)).foldLeft(true)(_ && _))
        {
          typeCheck(fun, tenv) match
            case ArrowT(tvars, paramTys, retTy) =>
              if ((tvars.length == tys.length) && (paramTys.length == args.length) && (args zip paramTys).map((e, t) => mustSame(typeCheck(e, tenv), subst(t, tvars, tys))).foldLeft(true)(_&&_))
                subst(retTy, tvars, tys)
              else error("11")
            case _ => error("12")
        }
      else error("13")
    // mutually recursive definitions
    case ERecDefs(defs: List[RecDef], body: Expr) =>
      def serialUpdate(ds: List[RecDef], te: TypeEnv): TypeEnv = ds match
        case x :: xs => serialUpdate(xs, tenvUpdate(x, te))
        case Nil => te
      val newTenv = serialUpdate(defs, tenv)
      val ty = typeCheck(body, newTenv)
      if (defs.forall(d => yaNot(d, newTenv)) && mustValid(ty, tenv)) ty else error("14")
    // pattern matching
    case EMatch(expr: Expr, mcases: List[MatchCase]) => typeCheck(expr, tenv) match
      case IdT(name, tys) =>
        tenv.tys.getOrElse(name, error("15")) match
          case TIAdt(tvars, variants) =>
            if (mcases.length != variants.size || mcases.map(m => m.name).distinct.length != mcases.map(m => m.name).length) error("16")
            else
              val bodyTys: List[Type] = mcases.map((mcase) =>
                val varParams: List[Param] = variants.getOrElse(mcase.name, error("17"))
                val newTenv: TypeEnv = tenv.addVars((mcase.params zip varParams).map((mp, vp) => (mp, subst(vp.ty, tvars, tys))))
                typeCheck(mcase.body, newTenv)
              )
              if (bodyTys.forall(t => mustSame(t, bodyTys.head))) bodyTys.head else error("18")
          case _ => error("19")
      case _ => error("20")
    // exit
    case EExit(ty: Type, expr: Expr) => if (mustValid(ty, tenv) && mustSame(typeCheck(expr, tenv), StrT)) ty else error("21")


  def mustValid(ty: Type, tenv: TypeEnv): Boolean = ty match
    case UnitT => true
    case NumT => true
    case BoolT => true
    case StrT => true
    case IdT(tn, Nil) => if (tenv.tys.contains(tn)) true else false
    case IdT(tn, ts: List[Type]) => tenv.tys(tn) match
      case TIAdt(tvars: List[String], variants: Map[String, List[Param]])
        => (tvars.length == ts.length)
          && variants.values.toList.map(e => (e.length == ts.length)).foldLeft(true)(_ && _)
          && ts.map(t => mustValid(t, tenv)).foldLeft(true)(_ && _)
      case _ => false
    case ArrowT(tvars: List[String], paramTys: List[Type], retTy: Type) =>
      val newTenv = tenv.addTypeVars(tvars)
      paramTys.forall(t => mustValid(t, newTenv)) && mustValid(retTy, newTenv)

  def mustSame(t1: Type, t2: Type): Boolean = (t1, t2) match
    case (UnitT, UnitT) => true
    case (NumT, NumT) => true
    case (BoolT, BoolT) => true
    case (StrT, StrT) => true
    case (IdT(name1: String, tys1: List[Type]), IdT(name2: String, tys2: List[Type])) =>
      (name1 == name2) && (tys1.length == tys2.length) && (tys1 zip tys2).forall((t1, t2) => mustSame(t1, t2))
    case (ArrowT(tvars1, paramTys1, retTy1), ArrowT(tvars2, paramTys2, retTy2)) =>
      ((tvars1.length == tvars2.length) && (paramTys1.length == paramTys2.length) &&
        (paramTys1 zip paramTys2).forall((t1, t2) => mustSame(t1, subst(t2, tvars2, tvars1.map(n => IdT(n, Nil))))))
    case _ => false

  def subst(bodyTy: Type, typeVars: List[String], typeArgs: List[Type]): Type = {
    val mapping: Map[String, Type] = (typeVars zip typeArgs).map((p, a) => p -> a).toMap
    bodyTy match
      case UnitT => UnitT
      case NumT => NumT
      case BoolT => BoolT
      case StrT => StrT
      case IdT(name: String, Nil) => mapping.getOrElse(name, IdT(name))
      case IdT(name: String, tys: List[Type]) => IdT(name, tys.map(t => subst(t, typeVars, typeArgs)).toList)
      case ArrowT(tvars: List[String], paramTys: List[Type], retTy: Type) =>
        val freeTvars: List[String] = typeVars.filterNot(tvars.toSet)
        ArrowT(tvars, paramTys.map(ty => subst(ty, freeTvars, typeArgs)), subst(retTy, freeTvars, typeArgs))
  }

  def tenvUpdate(d: RecDef, tenv: TypeEnv): TypeEnv = d match
    // immutable lazy variable definition
    case LazyVal(name: String, ty: Type, init: Expr) => tenv.addVar(name, ty)
    // recursive function
    case RecFun(name: String, tvars: List[String], params: List[Param], rty: Type, body: Expr) =>
      tenv.addVar(name, ArrowT(tvars, params.map(p => p.ty).toList, rty))
    // polymorphic algebraic data type
    case TypeDef(name: String, tvars: List[String], varts: List[Variant]) =>
      if (tenv.tys.contains(name)) error("22")
      else
        val tenv0 = tenv.addTypeName(name, tvars, varts)
        def serialUpdate(prevTenv: TypeEnv, ecase: List[Variant]): TypeEnv = ecase match
          case x :: xs => serialUpdate(prevTenv.addVar(x.name, ArrowT(tvars, x.params.map(p => p.ty), IdT(name, tvars.map(a => IdT(a))))), xs)
          case Nil => prevTenv
        serialUpdate(tenv0, varts)

  def yaNot(d: RecDef, tenv: TypeEnv): Boolean = d match
    // immutable lazy variable definition
    case LazyVal(name: String, ty: Type, init: Expr) =>
      mustValid(ty, tenv) && mustSame(ty, typeCheck(init, tenv))
    // recursive function
    case RecFun(name: String, tvars: List[String], params: List[Param], rty: Type, body: Expr) =>
      if (tvars.forall(a => !tenv.tys.contains(a))) {
        val newTenv: TypeEnv = tenv.addTypeVars(tvars)
        params.forall(p => mustValid(p.ty, newTenv))
          && mustValid(rty, newTenv)
          && mustSame(rty, typeCheck(body, newTenv.addVars(params.map(p => (p.name, p.ty)))))
      }
      else false
    // polymorphic algebraic data type
    case TypeDef(name: String, tvars: List[String], varts: List[Variant]) =>
      if (tvars.forall(a => !tenv.tys.contains(a))) {
        val newTenv: TypeEnv = tenv.addTypeVars(tvars)
        varts.forall(v => v.params.forall(p => mustValid(p.ty, newTenv)))
      }
      else false


  def interp(expr: Expr, env: Env): Value = expr match
    // unit
    case EUnit => UnitV
    // numbers
    case ENum(number: BigInt) => NumV(number)
    // booleans
    case EBool(bool: Boolean) => BoolV(bool)
    // strings
    case EStr(string: String) => StrV(string)
    // identifier lookups
    case EId(name: String) => env.getOrElse(name, error("23")) match
      case ExprV(body, eenv) => interp(body, eenv())
      case _ => env.getOrElse(name, error("24"))
    // addition
    case EAdd(left: Expr, right: Expr) => (interp(left, env), interp(right, env)) match
      case (NumV(l), NumV(r)) => NumV(l + r)
      case _ => error("25")
    // multiplication
    case EMul(left: Expr, right: Expr) => (interp(left, env), interp(right, env)) match
      case (NumV(l), NumV(r)) => NumV(l * r)
      case _ => error("26")
    // division
    case EDiv(left: Expr, right: Expr) => (interp(left, env), interp(right, env)) match
      case (NumV(l), NumV(r)) => if (r != 0) NumV(l / r) else error("27")
      case _ => error("28")
    // modulo
    case EMod(left: Expr, right: Expr) => (interp(left, env), interp(right, env)) match
      case (NumV(l), NumV(r)) => if (r != 0) NumV(l % r) else error("29")
      case _ => error("30")
    // string concatenation
    case EConcat(left: Expr, right: Expr) => (interp(left, env), interp(right, env)) match
      case (StrV(l), StrV(r)) => StrV(l + r)
      case _ => error("31")
    // equal-to
    case EEq(left: Expr, right: Expr) => BoolV(eq(interp(left, env), interp(right, env)))
    // less-than
    case ELt(left: Expr, right: Expr) => (interp(left, env), interp(right, env)) match
      case (NumV(l), NumV(r)) => BoolV(l < r)
      case _ => error("32")
    // sequence
    case ESeq(left: Expr, right: Expr) =>
      val v1 = interp(left, env)
      interp(right, env)
    // conditional
    case EIf(cond: Expr, thenExpr: Expr, elseExpr: Expr) => interp(cond, env) match
      case BoolV(true) => interp(thenExpr, env)
      case BoolV(false) => interp(elseExpr, env)
      case _ => error("33")
    // immutable variable definitions
    case EVal(x: String, tyOpt: Option[Type], expr: Expr, body: Expr) =>
      interp(body, env + (x -> interp(expr, env)))
    // anonymous (lambda) functions
    case EFun(params: List[Param], body: Expr) => CloV(params.map(p => p.name).toList, body, () => env)
    // function applications
    case EApp(fun: Expr, tys: List[Type], args: List[Expr]) => interp(fun, env) match
      case CloV(params, body, fenv) => interp(body, fenv() ++ (params zip args).map((p, a) => p -> interp(a, env)).toMap)
      case ConstrV(name) => VariantV(name, args.map(e => interp(e, env)))
      case _ => error("34")
    // mutually recursive definitions
    case ERecDefs(defs: List[RecDef], body: Expr) =>
      lazy val envn: Env = defs.foldLeft(env)(envUpdate(_, () => envn, _))
      interp(body, envn)
    // pattern matching
    case EMatch(expr: Expr, mcases: List[MatchCase]) =>
      def patternMatching(name: String, values: List[Value], cases: List[MatchCase]): Value = cases match
        case x :: xs => if (x.name == name && x.params.length == values.length)
                          interp(x.body, env ++ ((x.params zip values).map((p, v) => p -> v).toMap))  else patternMatching(name, values, xs)
        case Nil => error("35")
      interp(expr, env) match
        case VariantV(name, values) => patternMatching(name, values, mcases)
        case _ => error("36")
    // exit
    case EExit(ty: Type, expr: Expr) => error("37")


  def eq(left: Value, right: Value): Boolean = (left, right) match
    case (UnitV, UnitV) => true
    case (NumV(l), NumV(r)) => l == r
    case (BoolV(l), BoolV(r)) => l == r
    case (StrV(l), StrV(r)) => l == r
    case (VariantV(xl, vl: List[Value]), VariantV(xr, vr: List[Value])) =>
      (xl == xr) && (vl.length == vr.length) && (vl zip vr).forall((v1, v2) => eq(v1, v2))
    case _ => false

  def envUpdate(env1: Env, env2: () => Env, d: RecDef): Env = d match
    // immutable lazy variable definition
    case LazyVal(name: String, ty: Type, init: Expr) => env1 + (name -> ExprV(init, env2))
    // recursive function
    case RecFun(name: String, tvars: List[String], params: List[Param], rty: Type, body: Expr) =>
      env1 + (name -> CloV(params.map(p => p.name).toList, body, env2))
    // polymorphic algebraic data type
    case TypeDef(name: String, tvars: List[String], varts: List[Variant]) =>
      env1 ++ varts.map(v => v.name -> ConstrV(v.name)).toMap
}
