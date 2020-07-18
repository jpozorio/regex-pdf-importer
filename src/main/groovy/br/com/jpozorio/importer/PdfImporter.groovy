package br.com.jpozorio.importer

import br.com.jpozorio.importer.regex.RegexRecordToMatch
import groovy.transform.CompileStatic

import java.util.regex.Matcher

@CompileStatic
class PdfImporter<T> {

	final File file

	PdfImporter(File file) {
		this.file = file
	}

	List<T> readFile(final List<RegexRecordToMatch> regexRecordToMatchList) {
		final PdfToText pdfToText = new PdfToText(this.file)
		final Map<RegexRecordToMatch, ImportedRecordsList> importedRecordsByName = getListOfRecortsToImportation(regexRecordToMatchList)

		//todo find a intelligent way to do this replaces
		final String stringFromPdf = pdfToText.stringFromFile.replace(' ', ' ').replace('­', '-')
		int lineIndex = 0
		stringFromPdf.eachLine { final String line ->
			lineIndex++

			final String trimmedLine = line.trim()
			regexRecordToMatchList.each { final RegexRecordToMatch regexRecordToMatch ->
				final ImportedRecordsList recordsList = importedRecordsByName[regexRecordToMatch]
				final List<T> importedRecordsOfCurrent = recordsList.importedRecordsList
				final ImportedRecordPropertiesSetter propertiesSetter = new ImportedRecordPropertiesSetter(regexRecordToMatch)

				final Matcher matcher = regexRecordToMatch.pattern.matcher(trimmedLine)
				boolean matchHeader = regexRecordToMatch.patternHeader == null || (regexRecordToMatch.patternHeader != null && recordsList.matchedHeaderLineMatched == lineIndex - 1)

				if (matcher.matches() && matchHeader) {
					validateMatchGroupCountAndFieldNameSize(regexRecordToMatch, matcher)

					final T importedRecordInstance = getClazzInstance(regexRecordToMatch)
					importedRecordsOfCurrent.add(importedRecordInstance)

					propertiesSetter.setAllProperties(importedRecordInstance, matcher, importedRecordsByName)
					if (regexRecordToMatch.calculatedFields) {
						regexRecordToMatch.calculatedFields.call(importedRecordInstance)
					}
				}

				if (regexRecordToMatch.patternHeader != null) {
					final Matcher matcherHeader = regexRecordToMatch.patternHeader.matcher(trimmedLine)
					if (matcherHeader.matches()) {
						recordsList.matchedHeaderLineMatched = lineIndex
					} else {
						recordsList.matchedHeaderLineMatched = -1
					}
				}
			}
		}

		final List<T> importedRecordsList = createImportedRecordsList(importedRecordsByName)
		return importedRecordsList
	}

	private Map<RegexRecordToMatch, ImportedRecordsList> getListOfRecortsToImportation(List<RegexRecordToMatch> regexRecordToMatchList) {
		final Map<RegexRecordToMatch, ImportedRecordsList> importedRecordsByName = [:]

		regexRecordToMatchList.each { RegexRecordToMatch entry ->
			importedRecordsByName[entry] = new ImportedRecordsList([])
		}

		return importedRecordsByName
	}

	private T getClazzInstance(final RegexRecordToMatch regexRecordToMatch) {
		final Class<T> clazzRegistro = regexRecordToMatch.clazzRegistroImportado
		final T registroImportado = clazzRegistro.newInstance()
		return registroImportado
	}

	private List<T> createImportedRecordsList(final Map<RegexRecordToMatch, ImportedRecordsList<T>> importedRecordsByName) {
		final List<T> importedRecordsList = []
		importedRecordsByName.each { RegexRecordToMatch entry, ImportedRecordsList<T> recordsList ->
			if (entry.returnImportedRecord) {
				importedRecordsList.addAll(recordsList.importedRecordsList)
			}
		}
		return importedRecordsList
	}

	private void validateMatchGroupCountAndFieldNameSize(RegexRecordToMatch regexRecordToMatch, Matcher matcher) {
		if (regexRecordToMatch.fieldNames.size() != matcher.groupCount()) {
			throw new IllegalArgumentException("Matcher has ${matcher.groupCount()} groups, but fieldNames from record ${regexRecordToMatch.name} has ${regexRecordToMatch.fieldNames.size()}. They must be equals.")
		}
	}

}
