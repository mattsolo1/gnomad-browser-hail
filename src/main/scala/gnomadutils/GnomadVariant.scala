package gnomadutils

import is.hail.variant.{AltAllele, VariantDataset}
import is.hail.expr.{Field => HailField, Type => HailType, _}
import is.hail.annotations.Annotation
import is.hail.expr.JSONAnnotationImpex

import scala.collection.mutable.ArrayBuffer
import org.json4s._

case class GnomadVariant(
  contig: String,
  start: Long,
  ref: String,
  altAlleles: Seq[AltAllele],
  rawAnnotations: Annotation,
  annotations: JValue,
  integer: Int
) 

object GnomadVariant {
  def toGnomadVariants(vds: VariantDataset) = {
    val vas = vds.vaSignature

    val results = vds.rdd.map { case (v, (va, gs)) =>
      GnomadVariant(
        contig = v.contig,
        start = v.start,
        ref = v.ref,
        altAlleles = v.altAlleles,
        rawAnnotations = va,
        annotations = JSONAnnotationImpex.exportAnnotation(va, vas),
        integer = 5
//        population = getPopulationStats(vas, va)
      )
    }
    val collected = results.collect().toList
    collected
  }

//  getPopulationStats(vas: TStruct, va: Annotation) = {
//
//  }
}
