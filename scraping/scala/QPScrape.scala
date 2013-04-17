import javax.xml.parsers._
import java.io.StringReader
import java.io.StringWriter
import javax.xml.xpath._
import javax.xml.transform._
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import nu.xom._
import org.w3c.tidy.Tidy
import org.xml.sax._
import org.xml.sax.helpers._
import scala.io.Source
import util.Properties

object QPScrape {
	val queenpediaSongList = "http://queenpedia.com/index.php?title=Song_List"
	val queenpediaSongListLocal = "/Users/ddmoore/dev/miracle/scratch/songlist.html"
	val songXPath = "/html/body//div[@id=\"bodyContent\"]/(table/tr/td/ul/li/a | ul/li/a)"

	def main(args: Array[String]) = {
		// download the URL ourselves in case we need to clean it up with tidy, etc.
		val sb = new StringBuilder
		for (line <- Source.fromURL(queenpediaSongList).getLines)
		//for (line <- Source.fromFile(queenpediaSongListLocal).getLines)
			sb.append(line).append(Properties.lineSeparator)
		val songListHTML = sb.toString

		val songListHTMLTidy = cleanHTMLTidy(songListHTML)

		println("Tidied HTML: " + songListHTMLTidy)
		
		//parseSongListHTML(songListHTMLTidy)
	}
	
	def cleanHTML(html: String): String = {
        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl")

		val inputSource = new InputSource(new StringReader(html))
		val source = new DOMSource(DocumentBuilderFactory.newInstance.newDocumentBuilder.parse(inputSource))
		val writer = new StringWriter
		val result = new StreamResult(writer)
		TransformerFactory.newInstance.newTransformer.transform(source, result)
		
		return writer.toString
	}
	
	def cleanHTMLTidy(html: String): String = {
		// tidy up html contents		
		val reader = new StringReader(html)
		val writer = new StringWriter
		val tidy = new Tidy
		//tidy.setXHTML(true)
		//tidy.setQuiet(false)
		//tidy.setShowWarnings(false)
		//tidy.setOnlyErrors(true)
		tidy.setForceOutput(true)
		//println("Tidying HTML")
		tidy.parse(reader, writer)
		writer.toString	
	}
	
	def parseSongListHTML(html: String) = {
		/*
		val factory = DocumentBuilderFactory.newInstance
		val builder = factory.newDocumentBuilder
		val document = builder.parse(queenpediaSongList)
		*/
		
		// Setting tagsoup as SAX parser didn't work properly when using XPath API
		//System.setProperty("javax.xml.parsers.SAXParserFactory", "org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl");

		println("Parsing this HTML:\n\n" + html)
		
		val xpathFactory = XPathFactory.newInstance
		val xpath = xpathFactory.newXPath
		val inputSource = new InputSource(new StringReader(html))
		val songs = xpath.evaluate(songXPath, inputSource)
		
		println(songs)
	}
}
