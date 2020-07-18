package br.com.jpozorio.importer.strategy

import com.itextpdf.text.pdf.parser.LineSegment
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy
import com.itextpdf.text.pdf.parser.Matrix
import com.itextpdf.text.pdf.parser.TextRenderInfo

class HorizontalExtractionStrategy extends LocationTextExtractionStrategy {

	private static float DISTANCE_TO_CONSIDER_NEW_COLUMN = 1.00f
	protected final TextChunkLocationStrategy strat;

	HorizontalExtractionStrategy(TextChunkLocationStrategy strat) {
		super(strat)
		this.strat = strat
	}

	/** a summary of all found text */
	private final List<TextChunk> locationalResult = new ArrayList<TextChunk>();

	void renderText(TextRenderInfo renderInfo) {
		LineSegment segment = renderInfo.getBaseline();
		if (renderInfo.getRise() != 0) { // remove the rise from the baseline - we do this because the text from a super/subscript render operations should probably be considered as part of the baseline of the text the super/sub is relative to
			Matrix riseOffsetTransform = new Matrix(0, -renderInfo.getRise());
			segment = segment.transformBy(riseOffsetTransform);
		}
		TextChunk tc = new TextChunk(renderInfo.getText(), this.strat.createLocation(renderInfo, segment));
		locationalResult.add(tc);
	}

	public String getResultantText(TextChunkFilter chunkFilter) {
		List<TextChunk> filteredTextChunks = filterTextChunks(locationalResult, chunkFilter);
		Collections.sort(filteredTextChunks);

		StringBuilder sb = new StringBuilder();
		TextChunk lastChunk = null;
		for (TextChunk chunk : filteredTextChunks) {

			String text = chunk.text
			if (lastChunk == null) {
				sb.append(text);
			} else {
				if (chunk.sameLine(lastChunk)) {
					// we only insert a blank space if the trailing character of the previous string wasn't a space, and the leading character of the current string isn't a space
					float diff = chunk.getLocation().distanceFromEndOf(lastChunk.getLocation())
					boolean wordBoundary = isChunkAtWordBoundary(chunk, lastChunk)
//					if (diff > 1.0f && !startsWithSpace(chunk.text) && !endsWithSpace(lastChunk.text)) {
					if (diff > DISTANCE_TO_CONSIDER_NEW_COLUMN) {
						sb.append('|')
						text = text.trim()
					};

					sb.append(text);
				} else {
					sb.append('\n');
					sb.append(text);
				}
			}
			lastChunk = chunk;
		}

		return sb.toString();
	}

	private List<TextChunk> filterTextChunks(List<TextChunk> textChunks, TextChunkFilter filter) {
		if (filter == null) {
			return textChunks
		};

		List<TextChunk> filtered = new ArrayList<TextChunk>();
		for (TextChunk textChunk : textChunks) {
			if (filter.accept(textChunk)) {
				filtered.add(textChunk)
			};
		}
		return filtered;
	}

	private boolean startsWithSpace(String str) {
		if (str.length() == 0) {
			return false
		};
		return str.charAt(0) == ' ';
	}

	/**
	 * @param str
	 * @return true if the string ends with a space character, false if the string is empty or ends with a non-space character
	 */
	private boolean endsWithSpace(String str) {
		if (str.length() == 0) {
			return false
		};
		return str.charAt(str.length() - 1) == ' ';
	}

}
