import org.scalatest._

import is.hail.HailContext
import is.hail.expr.{Type, TChar, TString, TDouble, TSet, TInt, TArray, TDict, TVariant, TGenotype, TStruct, TCall, Parser, EvalContext}

class ConvertHailToGQL extends FlatSpec with Matchers {

  val hc = HailContext()


  "object" should "do something specific" in {

  }
}