import org.scalatest._
import is.hail.HailContext
import is.hail.expr.{EvalContext, Parser, TArray, TCall, TChar, TDict, TDouble, TGenotype, TInt, TSet, TString, TStruct, TVariant, Type}
import scala.collection.mutable.ArrayBuffer

import gnomadutils.GnomadVariant.{toGnomadVariants, toGraphQLField}


class ConvertHailToGQL extends FlatSpec with Matchers {

  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "Get variant data" should "package into new class" in {
    val gnomadVariants = toGnomadVariants(vds)
    val first = gnomadVariants.take(3)
//    first.foreach(println)
  }

  "toGraphQLField" should "take variant of annotation signature, convert to gql types" in {
    val vas = vds.vaSignature
    val Some(field) = vas.fieldOption(List("info"))
    val graphSchema = toGraphQLField(field)
    val last = true
    println(graphSchema)
  }
}
