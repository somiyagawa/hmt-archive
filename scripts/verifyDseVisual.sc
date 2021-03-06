// If you want to use sbt instead of almond.sh, comment out
// sections one and two.
//
// Almond.sh set up:
// 1. Add maven repository where we can find our libraries
val myBT = coursierapi.MavenRepository.of("https://dl.bintray.com/neelsmith/maven")
interp.repositories() ++= Seq(myBT)
// 2. Ivy import:
import $ivy.`edu.holycross.shot::scm:7.2.0`
import $ivy.`edu.holycross.shot::dse:7.0.0`
import $ivy.`edu.holycross.shot::citeobj:7.4.0`
import $ivy.`edu.holycross.shot.cite::xcite:4.2.0`
//
// Generic Scala:
import edu.holycross.shot.scm._
import edu.holycross.shot.cite._
import edu.holycross.shot.citeobj._
import edu.holycross.shot.dse._


// Configure values for source data and tools:
val releaseId = "2020e"
val url = s"https://raw.githubusercontent.com/homermultitext/hmt-archive/master/releases-cex/hmt-${releaseId}.cex"
val cexFile = s"releases-cex/hmt-${releaseId}.cex"
val  baseUrl : String  = "http://www.homermultitext.org/iipsrv?"
val  basePath: String = "/project/homer/pyramidal/deepzoom/"


// Use either of these:
//val hmtLib = CiteLibrarySource.fromUrl(url)
val hmtLib = CiteLibrarySource.fromFile(cexFile)

val dsev = DseVector.fromCiteLibrary(hmtLib)
val collrepo = hmtLib.collectionRepository.get


import java.io.PrintWriter
def verifyReport = {
  val tbsModel = Cite2Urn("urn:cite2:cite:datamodels.v1:tbsmodel")
  val codices = hmtLib.collectionsForModel(tbsModel)
  for (codex <- codices) {
    val seqUrn = codex.addProperty("sequence")
    println("Use codex's sequence property to sort: " + seqUrn)
    val pageSeq = collrepo.citableObjects.filter(obj => obj.urn.dropSelector == codex).sortBy(pg => pg.propertyValue(seqUrn).toString.toDouble).map(_.urn)
    val mdList = pageSeq.map(pg => {
      val ict = dsev.ictForSurface(pg)
      ict match {
        case None => "- Page " + pg.objectComponent
        case _ => "- Page [" + pg.objectComponent + s"](${ict.get})"
      }
    })
    new PrintWriter(s"verification-${codex.collection}-${releaseId}.md"){write(s"# Verify codex ${codex.collection}\n\n" + s"Verification applies to data release **${releaseId}**\n\n" + mdList.mkString("\n"));close;}
  }
}

println("To write a verification report on each configured codex:")
println("\tverifyReport")
