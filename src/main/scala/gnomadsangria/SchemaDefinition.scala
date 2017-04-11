package gnomadsangria

import is.hail.variant.VariantDataset

import sangria.schema.{fields, ObjectType, Field,
StringType, LongType, IntType, ListType, Argument, OptionType, Schema}

import gnomadutils.{GnomadGene, VdsVariant}

class SchemaDefinition(datasets: Map[String, VariantDataset]) {
  def getVdsVariantDefinition(typeName: String, vds: VariantDataset) = {
    val variantFields = fields[GnomadDatabase, VdsVariant](
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
        StringType,
        Some("Alternate allele"),
        resolve = _.value.altAlleles.map(altAllele => altAllele.toString).toList(0)
      )
    )
    val topLevelFields = List("pass", "rsid", "qual", "info", "vep")
    val annotationFields = topLevelFields.flatMap(field => ToGraphQL.makeGraphQLVariantSchema(vds, field))
    val variantType = ObjectType(typeName, variantFields ++ annotationFields)
    variantType
  }

  val ExomeVariantType = getVdsVariantDefinition("ExomeVariant", datasets("exome_variants"))
  val GenomeVariantType = getVdsVariantDefinition("GenomeVariant", datasets("genome_variants"))

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
        ListType(ExomeVariantType),
        Some("Exome variants"),
        resolve = (context) => {
          val gene = context.value
          val variants = context.ctx.getVariants("exome_variants", gene.chrom, gene.start, gene.stop)
          variants
        }
      ),
      Field(
        "genome_variants",
        ListType(GenomeVariantType),
        Some("Genome variants"),
        resolve = (context) => {
          val gene = context.value
          val variants = context.ctx.getVariants("genome_variants", gene.chrom, gene.start, gene.stop)
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
