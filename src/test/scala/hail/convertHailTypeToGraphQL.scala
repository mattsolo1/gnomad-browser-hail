import org.scalatest._
import is.hail.HailContext
import is.hail.expr.{EvalContext, Parser, TArray, TCall, TChar, TDict, TDouble, TGenotype, TInt, TSet, TString, TStruct, TVariant, Type}

import scala.collection.mutable.ArrayBuffer
//import hail.variant.Variant

case class GnomadVariant(
  contig: String,
  start: Long,
  ref: String,
  allele_count: Any
)

class ConvertHailToGQL extends FlatSpec with Matchers {

  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "Get variant data" should "package into new class" in {

    val vas = vds.vaSignature
    // val acField = vds.vaSignature.fieldOption(List("info", "AC"))

    val results = vds.rdd.map { case (v, (va, gs)) =>
      GnomadVariant(
        contig = v.contig,
        start = v.start,
        ref = v.ref,
        allele_count = vas.query("info", "AC")(va).asInstanceOf[ArrayBuffer[Int]].toList
      )
    }

    val collected = results.collect()
    val first = collected.take(1)
   collected.foreach(println)

  }
}
