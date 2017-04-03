package gnomadutils

import org.scalatest._
import is.hail.HailContext
import is.hail.expr.TStruct

class ProcessSpec extends FlatSpec with Matchers {
  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "vds.queryVA" should "do something specific" in {
    val (_, countQuery) = vds.queryVA("va.info.AC")
    val (_, assemblyQuery) = vds.queryVA("va.vep.assembly_name")
    val (_, consequenceQuery) = vds.queryVA("va.vep.transcript_consequences")
    val results = vds.variantsAndAnnotations
      .collect()
      .take(3)
      .map { case (v, va) =>
        Map(
          "counts" -> countQuery(va),
          "assemblies" -> assemblyQuery(va),
          "consequences" -> consequenceQuery(va)
        )
      }
    results.size should be (3)
//    results.foreach(println)
  }

  "QueryVA + TStruct" should "make a new schema from old values" in {
    val (countSig, countQuery) = vds.queryVA("va.info.AC")
    val (numberSig, numberQuery) = vds.queryVA("va.info.AN")
    val (freqSig, frequencyQuery) = vds.queryVA("va.info.AF")

    val countStatsSig = TStruct(
      "allele_count" -> countSig,
      "allele_num" -> numberSig,
      "allele_freq" -> freqSig
    )

    val (typ, inserter) = vds.insertVA(countStatsSig, "count_stats")
    vds.variantsAndAnnotations
      .take(3)
      .map { case (v, va) =>
//        inserter(va, )
      }
  }

  "test" should "print identity" in {
    Process.test("Hello there") should be ("Hello there")
  }

  "getMostSevereConsequence" should "get severe consequences" in {
    val results = vds.variantsAndAnnotations
      .take(3)
      .map { case (v, va) =>
//        println(Process.getMostSevereConsequence(vds, va))
      }
  }
}
