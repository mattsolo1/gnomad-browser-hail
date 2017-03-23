import org.scalatest._
import is.hail.HailContext
import is.hail.expr.{EvalContext, Parser, TArray, TCall, TChar, TDict, TDouble, TGenotype, TInt, TSet, TString, TStruct, TVariant, Type}
import scala.collection.mutable.ArrayBuffer

import gnomadutils.GnomadVariant.{toGnomadVariants}

class ConvertHailToGQL extends FlatSpec with Matchers {

  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "Get variant data" should "package into new class" in {

    val gnomadVariants = toGnomadVariants(vds)

    val first = gnomadVariants.take(3)
    first.foreach(println)

  }
}
