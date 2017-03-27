import org.scalatest._
import is.hail.HailContext
import is.hail.expr.{EvalContext, Parser, TArray, TCall, TChar, TDict, TDouble, TGenotype, TInt, TSet, TString, TStruct, TVariant, Type}

import scala.collection.mutable.ArrayBuffer
import org.json4s.{DefaultFormats, JValue}
import is.hail.expr.JSONAnnotationImpex
import gnomadutils.GnomadVariant.{toGnomadVariants}
import gnomadsangria.ToGraphQL.{getAnnotationValues, hailToGraphQLField}
import org.apache.spark.sql.catalyst.expressions.GenericRow


class ConvertHailToGQL extends FlatSpec with Matchers {

  implicit val formats = DefaultFormats // Brings in default date formats etc.

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
    val graphSchema = hailToGraphQLField(field)
    val last = true
//    println(graphSchema)
  }

  "getAnnotationValues" should "create a map with annotation values" in {
    val vas = vds.vaSignature
    val annotations = vds.rdd.map { case (v, (va, gs)) =>
      List(getAnnotationValues("annotations", va, vas)).toMap
    }.collect()

//   annotations.take(2).foreach(println)
  }

  "resolve" should "get annotation from jobject?" in {
    val vas = vds.vaSignature
    val annotationJSON = vds.rdd.map { case (v, (va, gs)) =>
      val json = JSONAnnotationImpex.exportAnnotation(va, vas)
      json
    }.collect()

   annotationJSON.take(2).map(json => (json \\ "AC").extract[List[Int]]).foreach(println)
  }

  "vaSig" should "access array of structs" in {
    val vas = vds.vaSignature
    val Some(field) = vds.vaSignature.fieldOption(List("vep", "transcript_consequences"))
    val transcriptConsequences = field.typ match {
      case TArray(elementType: TStruct) =>
        elementType.fields.map(f => {
          (f.name, f.typ)
        })
    }
    val results = vds.variantsAndAnnotations.collect().foreach { case (v, va) =>
      val transcriptAnnotations = vas.query("vep", "transcript_consequences")(va)

//      println(field.typ.query("allele_num")(transcriptAnnotations))
    }

  }

//  "parse in scope" should "do something specific" in {
//    val vas = vds.vaSignature
//    val Some(field) = vds.vaSignature.fieldOption(List("vep", "transcript_consequences"))
//    val transcriptConsequences = field.typ match {
//      case TArray(elementType: TStruct) =>
//        val stuff = elementType.fields.map(f => {
//          (f.name, f.typ)
//        }): _*
//        val ec = EvalContext(stuff)
//    }
//  }

}
