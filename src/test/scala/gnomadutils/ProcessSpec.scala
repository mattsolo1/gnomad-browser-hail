package gnomadutils

import org.scalatest._
import is.hail.HailContext
import is.hail.expr.{TInt, TStruct, Type}
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
  }

  "insert/delete" should "should construct a new schema from multiple queries" in {
    val newSchemaMap = Map(
      "allele_count" -> "va.info.AC[0]",
      "allele_number" -> "va.info.AN",
      "allele_freq" -> "va.info.AF[0]"
    )

    val stuff = newSchemaMap.map { case (key, path) =>
      val (signature, query) = vds.queryVA(path)
      val (typ, inserter) = vds.vaSignature.insert(signature, key)
      (signature, query, typ, inserter)
    }

    val vas = vds.vaSignature

    def addNewFieldstoSignature(schemaMap: Map[String, String], vds: VariantDataset) =
      schemaMap.foldLeft((vds, List[Querier](), List[Inserter]()))((acc, schema) => {
        val (key, path) = schema
        val (signature, query) = acc._1.queryVA(path)
        val (typ, inserter) = acc._1.vaSignature.insert(signature, key)
        (vds.copy(vaSignature = typ), acc._2 :+ query, acc._3 :+ inserter)
      })

    val bundle = addNewFieldstoSignature(newSchemaMap, vds)

    def addAnnotations(bundle: Tuple3[VariantDataset, List[Querier], List[Inserter]]) = {
      val fs = bundle._2.zip(bundle._3)
      val added = bundle._1.mapAnnotations((v, va, gs) =>
        fs.map{ case (querier, inserter) => inserter(va, querier(va))})
      val (schema1, deleter1) = added.deleteVA(List("vep"))
      val clearedVariants1 = added.mapAnnotations((v, va, gs) => deleter1(va)).copy(vaSignature = schema1)

      val (schema2, deleter2) = clearedVariants1.deleteVA(List("info"))
      val clearedVariants2 = clearedVariants1.mapAnnotations((v, va, gs) => deleter2(va)).copy(vaSignature = schema2)
      clearedVariants2
    }

    println(addAnnotations(bundle).vaSignature.toPrettyString())
  }
}
