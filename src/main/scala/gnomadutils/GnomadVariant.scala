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
  altAlleles: Seq[AltAllele],
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
        altAlleles = v.altAlleles,
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
    implicit val formats = DefaultFormats
    val HailField(name, typ, index, attrs) = hailField
    val gqlfield: Field[GnomadDatabase, GnomadVariant] = typ match {
      case TBoolean => Field(
        name,
        BooleanType,
        toGraphQLDescription(attrs),
       resolve = ctx => {
         val result = (ctx.value.annotations \\ name).extract[Boolean]
         if (result == null) false else result
       }
      )
      case TString => Field(
        name,
        StringType,
        toGraphQLDescription(attrs),
        resolve = ctx => {
          val result = (ctx.value.annotations \\ name).extract[String]
          if (result == null) "Nothing to see" else result
        }
      )
      case TInt => Field(
        name,
        IntType,
        toGraphQLDescription(attrs),
        resolve = ctx => {
          val result = (ctx.value.annotations \\ name).extract[Int]
          if (result == null) 0 else result
        }
      )
      case TDouble => Field(
        name,
        FloatType,
        toGraphQLDescription(attrs),
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
              toGraphQLDescription(attrs),
              resolve = ctx => (ctx.value.annotations \\ name).extract[ArrayBuffer[Int]]
            )
          case TDouble =>
            Field(
              name,
              ListType(FloatType),
              toGraphQLDescription(attrs),
              resolve = ctx => (ctx.value.annotations \\ name).extract[ArrayBuffer[Double]]
            )
          case TString =>
            Field(
              name,
              ListType(StringType),
              toGraphQLDescription(attrs),
              resolve = ctx => (ctx.value.annotations \\ name).extract[ArrayBuffer[String]]
            )
          case _ => Field(name, StringType , toGraphQLDescription(attrs), resolve = (ctx) => "nothing to see here")
        }
      }
      case TStruct(hailFields) => {
        Field(name, ObjectType(name, hailFields.map(f => toGraphQLField(f)).toList), toGraphQLDescription(attrs), resolve = (ctx) => ctx.value)
      }
      case _ => Field(name, StringType , toGraphQLDescription(attrs), resolve = (ctx) => "nothing to see here")
    }
    gqlfield
  }

  def toGraphQLDescription(attrs: Map[String, String]): Option[String] = attrs.get("Description")

  def makeGraphQLVariantSchema(vaSignature: HailType, a: String): List[Field[GnomadDatabase, GnomadVariant]]  = {
    val Some(field) = vaSignature.fieldOption(List(a))
    val fields = toGraphQLField(field)
    List(fields)
  }
}
