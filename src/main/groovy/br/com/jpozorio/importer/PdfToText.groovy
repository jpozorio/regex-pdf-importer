package br.com.jpozorio.importer

import br.com.jpozorio.importer.strategy.HorizontalExtractionStrategy
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.*
import groovy.transform.CompileStatic

@CompileStatic
class PdfToText {

	final File file
	final String password

	PdfToText(File file, String password) {
		this.file = file
		this.password = password
	}

	String getStringFromFile() {
		final PdfReader reader = new PdfReader(new FileInputStream(file), password.bytes)
		final PdfReaderContentParser parser = new PdfReaderContentParser(reader)
		final StringBuilder st = new StringBuilder()

		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			final HorizontalExtractionStrategy strategy1 = new HorizontalExtractionStrategy(
					new LocationTextExtractionStrategy.TextChunkLocationStrategy() {
						LocationTextExtractionStrategy.TextChunkLocation createLocation(TextRenderInfo renderInfo, LineSegment baseline) {
							return new LocationTextExtractionStrategy.TextChunkLocationDefaultImp(
									baseline.getStartPoint(),
									baseline.getEndPoint(),
									renderInfo.getSingleSpaceWidth()
							)
						}
					}
			)

			final TextExtractionStrategy strategy = parser.processContent(i, strategy1)
			st.append(strategy.getResultantText())
			st.append('\n')
		}

		reader.close()
		return st.toString()
	}
}
