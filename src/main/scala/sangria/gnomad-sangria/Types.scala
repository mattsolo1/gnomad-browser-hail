package gnomadsangria

case class GnomadVariant(
  contig: String,
  start: Long,
  ref: String,
  allele_count: List[Int]
)

case class GnomadGene(
  gene_name: String,
  gene_id: String,
  chrom: String,
  start: Int,
  stop: String
)
