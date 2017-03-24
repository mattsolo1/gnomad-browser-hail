package gnomadutils

import is.hail.variant.{AltAllele, VariantDataset}
import is.hail.expr.{Type => HailType, Field => HailField, _}
import is.hail.annotations.Annotation
import sangria.schema._

import scala.collection.mutable.ArrayBuffer
import gnomadsangria.GnomadDatabase

case class GnomadVariant(
  contig: String,
  start: Long,
  ref: String,
  pass: Boolean,
  altAlleles: Seq[AltAllele],
  allele_count: List[Int],
  allele_frequency: List[Double],
  allele_number: Int,
  vqslod: Double,
  gq_hist_alt: List[String],
  as_filter_status: List[List[String]],
  neg_train: Boolean,
  pos_train:  Boolean,
  values: Map[String, Any]
) {

  def getValue(name: String) = values.get(name)
}

object GnomadVariant {
  def toGnomadVariants(vds: VariantDataset) = {
    val vas = vds.vaSignature

    val results = vds.rdd.map { case (v, (va, gs)) =>
      GnomadVariant(
        contig = v.contig,
        start = v.start,
        ref = v.ref,
        pass = vas.query("pass")(va).asInstanceOf[Boolean],
        altAlleles = v.altAlleles,
        allele_count = vas.query("info", "AC")(va).asInstanceOf[ArrayBuffer[Int]].toList,
        allele_frequency = vas.query("info", "AF")(va).asInstanceOf[ArrayBuffer[Double]].toList,
        allele_number = vas.query("info", "AN")(va).asInstanceOf[Int],
        vqslod = vas.query("info", "VQSLOD")(va).asInstanceOf[Double],
        gq_hist_alt = vas.query("info", "GQ_HIST_ALT")(va).asInstanceOf[ArrayBuffer[String]].toList,
        as_filter_status = vas.query("info", "AS_FilterStatus")(va).asInstanceOf[ArrayBuffer[Set[String]]].toList.map(_.toList),
        neg_train = vas.query("info", "VQSR_NEGATIVE_TRAIN_SITE")(va).asInstanceOf[Boolean],
        pos_train = vas.query("info", "VQSR_POSITIVE_TRAIN_SITE")(va).asInstanceOf[Boolean],
        values = Map()
      )
    }

    val collected = results.collect().toList
    collected
  }

  def toGraphQLType(HailType: HailType) = {
    HailType match {
      case TInt => IntType
    }
  }

  def toGraphQLField(hailField: HailField): Field[GnomadDatabase, GnomadVariant] = {
    val HailField(name, typ, index, attrs) = hailField
    val gqlfield: Field[GnomadDatabase, GnomadVariant] = typ match {
      case TBoolean => Field(name, BooleanType, Some("test"), resolve = _.value.pass)
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
//    val q = vaSignature.query("info", "AN")
//    val anField = vaSignature.fieldOption(List("info", "AN"))
//    val Some(HailField(name, typ, index, attrs)) = anField
//
//    val fields: List[Field[GnomadDatabase, GnomadVariant]] =
//      List(Field(
//        name,
//        toGraphQLType(typ),
//        Some("test"),
//        resolve = _.value.allele_number
//      ))

    val Some(field) = vaSignature.fieldOption(List("info"))
    val fields = toGraphQLField(field)
    List(fields)
  }
}
