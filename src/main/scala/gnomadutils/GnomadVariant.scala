package gnomadutils

import is.hail.variant.{Variant, AltAllele, VariantDataset}
import is.hail.expr.{Field => HailField, Type => HailType, _}
import is.hail.annotations.Annotation
import is.hail.expr.JSONAnnotationImpex

import scala.collection.mutable.ArrayBuffer
import org.json4s._

case class VdsVariant(
  contig: String,
  start: Long,
  ref: String,
  altAlleles: Seq[AltAllele],
  annotations: JValue
)

object VdsVariant {
  def toVdsSchemaVariants(vds: VariantDataset): List[VdsVariant] = {
    val vas = vds.vaSignature
    val results = vds.rdd.map { case (v, (va, gs)) =>
      VdsVariant(
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
}

case class LossOfFunctionVariant (
  contig: String,
  start: Long,
  ref: String,
  altAlleles: Seq[AltAllele]
)

object LossOfFunctionVariant {
  def clearAnnotations(vds: VariantDataset) = {
    val toDelete = List("info", "vep")
    vds.deleteVA(toDelete)
  }

  def filterVariantsLossOfFunction(vds: VariantDataset) = {
    vds.filterVariants {
      case (v, va, gs) => v.ref == "G"
    }
  }

  def newVariants(vds: VariantDataset) = {
    val (emptySchema, deleter) = vds.deleteVA()
    val newVariants = vds.mapAnnotations((v, va, gs) => deleter(va)).copy(vaSignature = emptySchema)
    newVariants
  }
}
