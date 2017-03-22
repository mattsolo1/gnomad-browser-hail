import org.scalatest._
import is.hail.HailContext
import is.hail.annotations._
import is.hail.variant.{Genotype, Variant}

import scala.collection.mutable.ArrayBuffer

class VariantsCollectSpec extends FlatSpec with Matchers {

  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "variantsAndAnnotations toMap" should "should return a map (variant ->  variant_annotations)" in {

    val variantAnnotationMap = vds.variantsAndAnnotations.collect().take(1).toMap
    val firstVariant = Variant("1", 55505462, "A", "G")
    variantAnnotationMap.keys.toSeq(0) should be(firstVariant)
    variantAnnotationMap.contains(firstVariant) should be(true)
  }
}
