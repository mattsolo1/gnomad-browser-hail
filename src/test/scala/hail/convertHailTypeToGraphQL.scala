import org.scalatest._

import is.hail.HailContext
import is.hail.expr.{Type, TChar, TString, TDouble, TSet, TInt, TArray, TDict, TVariant, TGenotype, TStruct, TCall, Parser, EvalContext}

class ConvertHailToGQL extends FlatSpec with Matchers {

  val hc = HailContext()

  val gnomad_exomes_PCSK9 = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"

  val vds = hc.read(gnomad_exomes_PCSK9)
  val vas = vds.vaSignature

  "object" should "do something specific" in {
    def validFormatType(typ: Type): Boolean = {
      typ match {
        case TString => true
        case TChar => true
        case TDouble => true
        case TInt => true
        case TCall => true
        case TStruct(_) => true
        case _ => false
      }
    }

    vas.fieldOption("filters").foreach { f =>
      println(f)
    }

    println(vas.children)
    println(vas.schema)

    val isValid = validFormatType(vas)
    isValid should be (true)
  }
}