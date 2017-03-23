package gnomadutils

import is.hail.variant.{VariantDataset, AltAllele}

import scala.collection.mutable.ArrayBuffer

case class GnomadVariant(
  contig: String,
  start: Long,
  ref: String,
  pass: Boolean,
  altAlleles: Seq[AltAllele],
  allele_count: List[Int],
  allele_frequency: List[Double],
  allele_number: Int,
  vqslod: Double
)

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
        vqslod = vas.query("info", "VQSLOD")(va).asInstanceOf[Double]
      )
    }

    val collected = results.collect().toList
    collected
  }
}
