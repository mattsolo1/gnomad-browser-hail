// https://github.com/scalaj/scalaj-http

import org.scalatest._

import scalaj.http.{HttpResponse, Http}

import org.json4s.jackson.JsonMethods
import org.json4s.{JObject, JArray, JInt, JString, JDecimal}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import is.hail.HailContext

import gnomadutils.FilterByInterval.{filterVariantsByInterval}

class GetIntervalWithAPITest extends FlatSpec with Matchers {

  implicit val formats = DefaultFormats
  val hc = HailContext()

  "gnomad API" should "be called to filter intervals with Hail" in {
    val url = "http://gnomad-api.broadinstitute.org"
    val query = """{
      gene(gene_name: "SRY") {
        gene_id
        gene_name
        chrom
        start
        stop
      }
    }"""

    val headers = Seq(("content-type", "application/graphql"), ("accept", "application/json"))
    val response = Http(url).postData(query).headers(headers).asString
    val json = response.body
    val parsed = JsonMethods.parse(json, useBigDecimalForDouble = true)
    val data = parsed.children(0).children(0)
    val gene = data.extract[Gene]
    // val interval_PCSK9 = "1:55505221-55530525"
    val gnomadExomeAutosomes = "/Users/msolomon/Data/gnomad/release-170228/gnomad.exomes.r2.0.1.sites.Y.vds"
    val intervalString = s"${gene.chrom}:${gene.start}-${gene.stop}"
    val filteredVDS = filterVariantsByInterval(hc, intervalString, gnomadExomeAutosomes)
    filteredVDS.count().nVariants should be (44)
  }
}
