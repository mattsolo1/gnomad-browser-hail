package gnomadutils

import org.scalatest._

import is.hail.HailContext
import is.hail.expr.{TInt, TStruct}

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

    val countStatsSig = TStruct(
      "allele_count" -> countSig,
      "allele_num" -> numberSig,
      "allele_freq" -> freqSig
    )

    val (typ, inserter) = vds.vaSignature.insert(countSig, "allele_count")
    val newVds = vds.mapAnnotations((v, va, gs) => inserter(va, countQuery(va)))
      .copy(vaSignature = typ)

    //    println(newVds.vaSignature.toPrettyString())

    val (schema, deleter) = newVds.deleteVA(List("vep"))
    val clearedVariants = newVds.mapAnnotations((v, va, gs) => deleter(va)).copy(vaSignature = schema)
  }
}
