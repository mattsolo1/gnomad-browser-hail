package gnomadsangria

import is.hail.variant.{Variant, AltAllele}

case class GnomadVariant(
  contig: String,
  start: Long,
  ref: String,
  altAlleles: Seq[AltAllele],
  allele_count: List[Int],
  allele_frequency: List[Double],
  allele_number: Int
)

case class GnomadGene(
  gene_name: String,
  gene_id: String,
  chrom: String,
  start: Int,
  stop: String
)
