import org.scalatest._

import scalaj.http.{HttpResponse, Http}
import org.json4s._
import org.json4s.jackson.JsonMethods._

case class Gene(gene_name: String, gene_id: String, chrom: String, start: Int, stop: String)

class ExtractValuesFromApi extends FlatSpec with Matchers {
  implicit val formats = DefaultFormats // Brings in default date formats etc.
  "json4s" should "extract values from graphql response" in {
    val url = "http://gnomad-api.broadinstitute.org"
    val query = """{
      gene(gene_name: "PCSK9") {
        stop
        gene_name
        gene_id
        chrom
        start
        gene_name
      }
    }"""
    val headers = Seq(("content-type", "application/graphql"), ("accept", "application/json"))
    val response = Http(url).postData(query).headers(headers).asString
    val json = parse(response.body)
    val data = json.children(0).children(0)
    val PCSK9_data = data.extract[Gene]

    PCSK9_data should be (Gene("PCSK9","ENSG00000169174","1",55505222,"55530526"))
  }
}
