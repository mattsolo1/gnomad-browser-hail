package gnomadutils

import is.hail.variant.{AltAllele, VariantDataset}
import is.hail.expr.{Field => HailField, Type => HailType, _}
import is.hail.annotations.Annotation
import is.hail.expr.JSONAnnotationImpex
import org.apache.spark.sql.Row
import sangria.schema._

import scala.collection.mutable.ArrayBuffer
import gnomadsangria.GnomadDatabase
import org.json4s.{DefaultFormats, JValue}

case class GnomadVariant(
  contig: String,
  start: Long,
  ref: String,
//  pass: Boolean,
 altAlleles: Seq[AltAllele],
//  allele_count: List[Int],
//  allele_frequency: List[Double],
//  allele_number: Int,
//  vqslod: Double,
//  gq_hist_alt: List[String],
//  as_filter_status: List[List[String]],
//  neg_train: Boolean,
//  pos_train:  Boolean,
//  values: Map[String, Any],
  annotations: JValue
) {
}

object GnomadVariant {
  def toGnomadVariants(vds: VariantDataset) = {
    val vas = vds.vaSignature

    val results = vds.rdd.map { case (v, (va, gs)) =>
      GnomadVariant(
        contig = v.contig,
        start = v.start,
        ref = v.ref,
//        pass = vas.query("pass")(va).asInstanceOf[Boolean],
       altAlleles = v.altAlleles,
//        allele_count = vas.query("info", "AC")(va).asInstanceOf[ArrayBuffer[Int]].toList,
//        allele_frequency = vas.query("info", "AF")(va).asInstanceOf[ArrayBuffer[Double]].toList,
//        allele_number = vas.query("info", "AN")(va).asInstanceOf[Int],
//        vqslod = vas.query("info", "VQSLOD")(va).asInstanceOf[Double],
//        gq_hist_alt = vas.query("info", "GQ_HIST_ALT")(va).asInstanceOf[ArrayBuffer[String]].toList,
//        as_filter_status = vas.query("info", "AS_FilterStatus")(va).asInstanceOf[ArrayBuffer[Set[String]]].toList.map(_.toList),
//        neg_train = vas.query("info", "VQSR_NEGATIVE_TRAIN_SITE")(va).asInstanceOf[Boolean],
//        pos_train = vas.query("info", "VQSR_POSITIVE_TRAIN_SITE")(va).asInstanceOf[Boolean],
//        values = Map(),
        annotations = JSONAnnotationImpex.exportAnnotation(va, vas)
      )
    }

    val collected = results.collect().toList
    collected
  }


  def getAnnotationValues(name: String, va: Annotation, hailType: HailType): Tuple2[String, Any] = {
    hailType match {
      case TBoolean => (name, va.asInstanceOf[Boolean])
      case TStruct(fields) =>
        val row = va.asInstanceOf[Row]
        (name , fields.map(f => getAnnotationValues(f.name, row.get(f.index), f.typ)).toMap)
      case _  => (name, "nothing yet")
    }
  }

  def toGraphQLField(hailField: HailField): Field[GnomadDatabase, GnomadVariant] = {
    implicit val formats = DefaultFormats // Brings in default date formats etc.
    val HailField(name, typ, index, attrs) = hailField
    val gqlfield: Field[GnomadDatabase, GnomadVariant] = typ match {
      case TBoolean => Field(
        name,
        BooleanType,
        Some("test"),
       resolve = ctx => {
         val result = (ctx.value.annotations \\ name).extract[Boolean]
         if (result == null) false else result
       }
      )
      case TString => Field(
        name,
        StringType,
        Some("test"),
        resolve = ctx => {
          val result = (ctx.value.annotations \\ name).extract[String]
          if (result == null) "Nothing to see" else result
        }
      )
      case TInt => Field(
        name,
        IntType,
        Some("test"),
        resolve = ctx => {
          val result = (ctx.value.annotations \\ name).extract[Int]
          if (result == null) 0 else result
        }
      )
      case TDouble => Field(
        name,
        FloatType,
        Some("test"),
        resolve = ctx => {
          val result = (ctx.value.annotations \\ name).extract[Double]
          if (result == null) 0.0 else result
        }
      )
      case TArray(elementType) => {
        println(elementType)
        elementType match {
          case TInt =>
            Field(
              name,
              ListType(IntType),
              Some("test"),
              resolve = ctx => (ctx.value.annotations \\ name).extract[ArrayBuffer[Int]]
            )
          case TDouble =>
            Field(
              name,
              ListType(FloatType),
              Some("test"),
              resolve = ctx => (ctx.value.annotations \\ name).extract[ArrayBuffer[Double]]
            )
          case TString =>
            Field(
              name,
              ListType(StringType),
              Some("test"),
              resolve = ctx => (ctx.value.annotations \\ name).extract[ArrayBuffer[String]]
            )
          case _ => Field(name, StringType , Some("test"), resolve = (ctx) => "nothing to see here")
        }
      }
      case TStruct(hailFields) => {
        Field(name, ObjectType(name, hailFields.map(f => toGraphQLField(f)).toList), Some("test"), resolve = (ctx) => ctx.value)
      }
      case _ => Field(name, StringType , Some("test"), resolve = (ctx) => "nothing to see here")
    }
    gqlfield
  }

//  def toGraphQLDescription(attrs: Map[String, Any]) =

  def toGraphQLDescription(attrs: String) = attrs

  def makeGraphQLVariantSchema(vaSignature: HailType): List[Field[GnomadDatabase, GnomadVariant]]  = {
    val Some(field) = vaSignature.fieldOption(List("info"))
    val fields = toGraphQLField(field)
    List(fields)
  }
}
