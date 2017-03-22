import org.scalatest._

import is.hail.HailContext
import is.hail.expr.{Type, TChar, TString, TDouble, TSet, TInt, TArray, TDict, TVariant, TGenotype, TStruct, TCall, Parser, EvalContext}
//import hail.variant.Variant

case class GnomadVariant(
  contig: String,
  start: Long,
  ref: String
)

class ConvertHailToGQL extends FlatSpec with Matchers {

  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "Get variant data" should "package into new class" in {

    // val q = vds.vaSignature.query("info", "AC")
    // val acField = vds.vaSignature.fieldOption(List("info", "AC"))

    val results = vds.rdd.map { case (v, (va, gs)) =>
      GnomadVariant(
        contig = v.contig,
        start = v.start,
        ref = v.ref
      )
    }

    val collected = results.collect()
//    collected.foreach(println)
  }
}
