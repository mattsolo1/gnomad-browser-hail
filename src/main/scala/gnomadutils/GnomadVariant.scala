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
  vqslod: Double,
  gq_hist_alt: List[String],
  as_filter_status: List[List[String]]
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
        vqslod = vas.query("info", "VQSLOD")(va).asInstanceOf[Double],
        gq_hist_alt = vas.query("info", "GQ_HIST_ALT")(va).asInstanceOf[ArrayBuffer[String]].toList,
        as_filter_status = vas.query("info", "AS_FilterStatus")(va).asInstanceOf[ArrayBuffer[Set[String]]].toList.map(_.toList)
      )
    }

    val collected = results.collect().toList
    collected
  }
}
