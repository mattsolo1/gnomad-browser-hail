package gnomadutils

import org.scalatest._

import is.hail.HailContext

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

  "test" should "print identity" in {
    Process.test("Hello there") should be ("Hello there")
  }

  "getMostSevereConsequence" should "get severe consequences" in {
    val results = vds.variantsAndAnnotations
      .collect()
      .take(3)
      .map {  case (v, va) =>
        println(Process.getMostSevereConsequence(vds, va))
      }
  }
}
