// case class SangriaVariant(variant_name: String, variant_id: String, rsid: String)
//
// class DataContext(data: List[(Variant, (Annotation, Iterable[Genotype]))]) {
//
//   val sangriaVariants: List[SangriaVariant] = data.map {
//     case (v, (va, gs)) => SangriaVariant(
//       variant_name = v.toString,
//       variant_id = v.toString,
//       rsid = "nothing"
//     )
//   }
//
//   def getAllVariants(): List[SangriaVariant] = { sangriaVariants }
//
//   def getRealVariant(variant_id: String): Option[SangriaVariant] =
//     sangriaVariants.find(variant => variant.variant_id == variant_id)
// }
//
// object GnomadSchema {
//   val SangriaVariantType = ObjectType("SangriaVariant", fields[Unit, SangriaVariant](
//     Field("variant_name",
//       StringType,
//       Some("The name of the variant"),
//       resolve = _.value.variant_name),
//     Field("rsid",
//       StringType,
//       Some("rsid"),
//       resolve = _.value.rsid),
//     Field("variant_id",
//       StringType,
//       Some("The variant id"),
//       resolve = _.value.variant_id)
//   ))
//
//   val Query = ObjectType("Query", fields[DataContext, Unit](
//     Field("variants", OptionType())
//   ))
//
//   val gSchema = Schema(SangriaVariantType)
// }
