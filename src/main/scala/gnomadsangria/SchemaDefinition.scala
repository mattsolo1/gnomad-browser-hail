package gnomadsangria

import sangria.schema._

import gnomadutils.{GnomadVariant, GnomadGene}

object SchemaDefinition {
  val VariantType = ObjectType(

    "Variant", fields[Unit, GnomadVariant](
      Field(
        "contig",
        StringType,
        Some("Chromosome"),
        resolve = _.value.contig
      ),
      Field(
        "start",
        LongType,
        Some("Start position"),
        resolve = _.value.start
      ),
      Field(
        "ref",
        StringType,
        Some("Reference allele"),
        resolve = _.value.ref
      ),
      Field(
        "alt",
        ListType(StringType),
        Some("Alternate allele"),
        resolve = _.value.altAlleles.map(altAllele => altAllele.toString).toList
      ),
      Field(
        "allele_count",
        ListType(IntType),
        Some("Allele count"),
        resolve = _.value.allele_count
      ),
      Field(
        "allele_frequency",
        ListType(FloatType),
        Some("Allele frequency"),
        resolve = _.value.allele_frequency
      ),
      Field(
        "allele_number",
        IntType,
        Some("Allele number"),
        resolve = _.value.allele_number
      ),
      Field(
        "vqslod",
        FloatType,
        Some("VQSLOD"),
        resolve = _.value.vqslod
      ),
      Field(
        "gq_hist_alt",
        ListType(StringType),
        Some("GQ histogram for alt allele"),
        resolve = _.value.gq_hist_alt
      ),
      Field(
        "as_filter_status",
        ListType(ListType(StringType)),
        Some("AS filter status"),
        resolve = _.value.as_filter_status
      )
    )
  )

  val GeneType = ObjectType(

    "Gene", fields[GnomadDatabase, GnomadGene](
      Field(
        "gene_name",
        StringType,
        Some("The name of the gene"),
        resolve = _.value.gene_name
      ),
      Field(
        "gene_id",
        StringType,
        Some("The gene id"),
        resolve = _.value.gene_id
      ),
      Field(
        "chrom",
        StringType,
        Some("The gene id"),
        resolve = _.value.chrom
      ),
      Field(
        "start",
        IntType,
        Some("Gene start position"),
        resolve = _.value.start
      ),
      Field(
        "stop",
        StringType,
        Some("Gene stop position"),
        resolve = _.value.stop
      ),
      Field(
        "exome_variants",
        ListType(VariantType),
        Some("Exome variants"),
        resolve = (context) => {
          val gene = context.value
          val variants = context.ctx.getVariants(gene.chrom, gene.start, gene.stop)
          variants
        }
      )
    )
  )

  val GeneNameArgument = Argument("gene_name", StringType, description = "Gene name")

  val Query = ObjectType("Query", fields[GnomadDatabase, Unit](
    Field("gene", OptionType(GeneType),
      arguments = GeneNameArgument :: Nil,
      resolve = (ctx) => ctx.ctx.getGene(ctx.arg(GeneNameArgument)))))

  val GnomadSchema = Schema(Query)
}
