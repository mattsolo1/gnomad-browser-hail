import org.scalatest._
import is.hail.HailContext
import is.hail.expr.Field
import is.hail.variant.Variant
import is.hail.expr.SparkAnnotationImpex


class VariantsSigSpec extends FlatSpec with Matchers {

  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "TStruct query" should "can query RDD row" in {

    val q = vds.vaSignature.query("info", "AC")
    val ac_strings = vds.rdd.map { case (v, (va, gs)) =>
      s"Variant: ${v}, Allele counts: ${q(va)}"
    }
    ac_strings.take(1) should be (Array("Variant: 1:55505462:A:G, Allele counts: ArrayBuffer(1)"))
  }

  "SparkAnnotationImpex.exportAnnotation" should "converts hail types to spark types?" in {

//    val sparkRDD = vds.rdd.map { case (v, (va, gs)) =>
//      SparkAnnotationImpex.exportAnnotation(va, vds.vaSignature)
//    }

//    sparkRDD.take(1).foreach(println)
  }

  "Annotation field" should "be bundled with data" in {

    val q = vds.vaSignature.query("info", "AC")
    val acField = vds.vaSignature.fieldOption(List("info", "AC"))

    val bundle = acField match {
      case Some(Field(name, typ, index, attrs)) =>
        vds.rdd.map { case (v, (va, gs)) =>
          Map(
            "variantId" -> v.toString(),
            "fieldName" -> name,
            "fieldValue" -> q(va),
            "hailType" -> typ,
            "fieldIndex" -> index,
            "attributes" -> attrs
          )
        }
     }
    val first = bundle.take(1)
    bundle.take(1)(0).get("variantId") should be (Some("1:55505462:A:G"))
    bundle.take(1)(0).get("fieldName") should be (Some("AC"))
    // bundle.count _ should be (1135)
  }
}
