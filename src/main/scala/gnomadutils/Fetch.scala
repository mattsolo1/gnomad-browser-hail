package gnomadutils

import scalaj.http.{HttpResponse, Http}

import org.json4s.{JObject, JArray, JInt, JString, JDecimal}
import org.json4s._
import org.json4s.jackson.JsonMethods._

object FetchData {
  implicit val formats = DefaultFormats

  val gnomadApiUrl = "http://gnomad-api.broadinstitute.org"

  def fetchGeneByName(geneName: String, url: String = gnomadApiUrl): GnomadGene = {

    val geneQuery = s"""{
      gene(gene_name: "${geneName}") {
        gene_id
        gene_name
        chrom
        start
        stop
      }
    }"""

    val headers = Seq(("content-type", "application/graphql"), ("accept", "application/json"))
    val response = Http(url).postData(geneQuery).headers(headers).asString
    val json = parse(response.body)
    val data = json.children(0).children(0)
    val extracted = data.extract[GnomadGene]
    extracted
  }
}
