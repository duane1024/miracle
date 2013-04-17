import java.io.{StringWriter, StringReader}
import javax.xml.transform.sax.SAXSource
import scala.io.Source
import scala.language.implicitConversions
import scala.collection.convert.WrapAsScala.iterableAsScalaIterable
import util.Properties

import org.w3c.tidy.Tidy
import org.xml.sax.InputSource
import net.sf.saxon.Configuration
import net.sf.saxon.s9api.{Processor, QName, XdmItem, XdmNode}

object WebScraper {
  val queenpediaSongList = "http://queenpedia.com/index.php?title=Song_List"
  val queenpediaSongListLocal = "/Users/ddmoore/dev/miracle/scratch/songlist.html"
  val songXPath = "/html/body//div[@id=\"bodyContent\"]/(table/tr/td/ul/li/a | ul/li/a)"

  def main(args: Array[String]) {
    // download the URL ourselves in case we need to clean it up with tidy, etc.
    val sb = new StringBuilder

    //for (line <- Source.fromFile(queenpediaSongListLocal).getLines)
    for (line <- Source.fromURL(queenpediaSongList).getLines())
      sb.append(line).append(Properties.lineSeparator)
    val songListHTML = sb.toString()
    val songListHTMLTidy = cleanHTMLTidy(songListHTML)

    parseSongListHTML(songListHTMLTidy)
  }

  def cleanHTMLTidy(html: String): String = {
    // tidy up html contents
    val reader = new StringReader(html)
    val writer = new StringWriter
    val tidy = new Tidy
    //tidy.setXHTML(true)
    tidy.setQuiet(true)
    tidy.setShowWarnings(false)
    tidy.setForceOutput(true)
    tidy.parse(reader, writer)
    writer.toString
  }

  def parseSongListHTML(html: String) {
    val config = Configuration.newConfiguration
    val processor = new Processor(config)
    val documentBuilder = processor.newDocumentBuilder
    val source = new SAXSource(new InputSource(new StringReader(html)))
    val doc = documentBuilder.build(source)

    val compiler = processor.newXPathCompiler
    compiler.declareNamespace("", "http://www.w3.org/1999/xhtml")
    val executable = compiler.compile(songXPath)
    val selector = executable.load
    selector.setContextItem(doc)
    val xpathResult = selector.evaluate

    var count = 0
    for (item: XdmItem <- xpathResult) {
      if (item.isInstanceOf[XdmNode]) {
        val node: XdmNode = item.asInstanceOf[XdmNode]
        val hrefAttr = new QName("href")
        val songurl = node.getAttributeValue(hrefAttr)
        val songnameText = node.getStringValue
        val songname = songnameText.replace('\n', ' ')
        println("song name = " + songname + ", song url = " + songurl)
        count = count + 1
      }
    }
  }
}
