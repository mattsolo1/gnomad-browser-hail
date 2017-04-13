import org.scalatest._
import is.hail.HailContext
import is.hail.expr.TSet

class QueryVariantSpec extends FlatSpec with Matchers {
  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "vds.queryVariants" should "take an array of expressions, return results" in {
    val expressions = Array(
	     "variants.map(v => v.ref).collect().toSet()"
    )
    val results = vds.queryVariants(expressions)
    results(0)._1.asInstanceOf[Set[String]].size should be (41)
    results(0)._2.isInstanceOf[TSet] should be (true)
  }

  "vds.queryVA" should "do something specific" in {
    val (_, countQuery) = vds.queryVA("va.info.AC")
//    val (_, assemblyQuery) = vds.queryVA("va.vep.assembly_name")
//    val (_, consequenceQuery) = vds.queryVA("va.vep.transcript_consequences")
    val results = vds.variantsAndAnnotations
      .collect()
      .map { case (v, va) =>
        Map(
          "counts" -> countQuery(va)
//          "assemblies" -> assemblyQuery(va),
//          "consequences" -> consequenceQuery(va)
        )
      }

      results.size should be (3)

    results.foreach(println)
  }
}
