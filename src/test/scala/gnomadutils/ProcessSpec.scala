package gnomadutils

import org.scalatest._
import is.hail.HailContext
import is.hail.expr.{Field, TArray, TInt, TStruct, Type}
import is.hail.annotations.{Inserter, Querier}
import is.hail.variant.VariantDataset

class ProcessSpec extends FlatSpec with Matchers {
  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "insert" should "can add new annotations" in {
    val (emptySchema, deleter) = vds.deleteVA()
    val clearedVariants = vds.mapAnnotations((v, va, gs) => deleter(va)).copy(vaSignature = emptySchema)
    val integerToAdd = 42
    val integerToAddSignature = TInt
    val (signature, inserter) = clearedVariants.vaSignature.insert(integerToAddSignature, "coolInteger")
    val withInt = clearedVariants.mapAnnotations((v, va, gs) => inserter(va, integerToAdd))
      .copy(vaSignature = signature)
    // println(withInt.vaSignature.toPrettyString())
  }

  "insert" should "add new annotations w. query" in {
    val query = vds.vaSignature.query("info", "AC")
    val field = vds.vaSignature.fieldOption(List("info", "AC"))
    val typ = field match {
      case Some(Field(name, typ, index, attrs)) => typ
    }
//    val integerToAddSignature = TInt
    val (signature, inserter) = vds.vaSignature.insert(typ, "allele_count")
    val withInt = vds.mapAnnotations((v, va, gs) => inserter(va, query(va)))
      .copy(vaSignature = signature)
//    println(withInt.vaSignature.toPrettyString())
//    withInt.rdd.collect().take(3).toList.foreach(println)
  }


  "deleteVA" should "can delete old schema and fill new schema" in {
    val (emptySchema, deleter) = vds.deleteVA()
    val newVariants = vds.mapAnnotations((v, va, gs) => deleter(va)).copy(vaSignature = emptySchema)
    //    println(newVariants.countVariants())
    //    newVariants.rdd.collect().take(3).toList
  }

  "insert/delete" should "make a new vds/schema from old vds" in {
    val (countSig, countQuery) = vds.queryVA("va.info.AC[0]")
    val (numberSig, numberQuery) = vds.queryVA("va.info.AN")
    val (freqSig, frequencyQuery) = vds.queryVA("va.info.AF")

    // val countStatsSig = TStruct(
    //   "allele_count" -> countSig,
    //   "allele_num" -> numberSig,
    //   "allele_freq" -> freqSig
    // )

    val (typ, inserter) = vds.vaSignature.insert(countSig, "allele_count")
    val newVds = vds.mapAnnotations((v, va, gs) => inserter(va, countQuery(va)))
      .copy(vaSignature = typ)

    //    println(newVds.vaSignature.toPrettyString())

    val (schema, deleter) = newVds.deleteVA(List("vep"))
    val clearedVariants = newVds.mapAnnotations((v, va, gs) => deleter(va)).copy(vaSignature = schema)
//    clearedVariants.rdd.collect().take(3).toList.foreach(println)
  }

  "insert/delete" should "should construct a new schema from multiple queries" in {
    val newSchemaMap = List(
      ("allele_count", List("info", "AC")),
      ("allele_number", List("info", "AN")),
      ("allele_frequency", List("info", "AF"))
    )

    val processed = Process.addAnnotations(vds, newSchemaMap)
    println(processed.vaSignature.toPrettyString())
    // processed.rdd.collect().take(3).toList.foreach(println)
  }

  "split" should "consequences into smaller bits" in {
    val test = List(
      "T|5_prime_UTR_variant|MODIFIER|PCSK9|ENSG00000169174|Transcript|ENST00000302118|protein_coding|1/12||ENST00000302118.5:c.-48C>T||243||||||1||1||SNV|1|HGNC|20001|YES|||CCDS603.1|ENSP00000303208|Q8NBP7||UPI00001615E1|1|||||||||||||||||||||||||||||||||",
      "T|5_prime_UTR_variant|MODIFIER|PCSK9|ENSG00000169174|Transcript|ENST00000452118|protein_coding|1/6||ENST00000452118.2:c.-48C>T||33||||||1||1||SNV|1|HGNC|20001|||||ENSP00000401598||B4DEZ9|UPI00017A6F55|1|||||||||||||||||||||||||||||||||",
      "T|upstream_gene_variant|MODIFIER|PCSK9|ENSG00000169174|Transcript|ENST00000543384|protein_coding|||||||||||1|432|1||SNV|1|HGNC|20001|||||ENSP00000441859||F5GWF0|UPI000206501F|1|||||||||||||||||||||||||||||||||",
      "T|regulatory_region_variant|MODIFIER|||RegulatoryFeature|ENSR00001037993|promoter|||||||||||1||||SNV|1||||||||||||||||||||||||||||||||||||||||||||"
    )
    println(test(0))
    test(0).split("\\|").foreach(println)
  }
}
