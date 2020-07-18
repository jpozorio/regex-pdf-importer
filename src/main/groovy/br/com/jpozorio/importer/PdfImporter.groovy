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
		final Map<RegexRecordToMatch, List<T>> importedRecordsByName = getListOfRecortsToImportation(regexRecordToMatchList)

		//todo find a intelligent way to do this replaces
		final String stringFromPdf = pdfToText.stringFromFile.replace(' ', ' ').replace('­', '-')
		stringFromPdf.eachLine { final String line ->
			final String trimmedLine = line.trim()
			regexRecordToMatchList.each { final RegexRecordToMatch regexRecordToMatch ->
				final List<T> importedRecordsOfCurrent = importedRecordsByName[regexRecordToMatch]
				final ImportedRecordPropertiesSetter propertiesSetter = new ImportedRecordPropertiesSetter(regexRecordToMatch)

				final Matcher matcher = regexRecordToMatch.pattern.matcher(trimmedLine)
				if (matcher.matches()) {
					validateMatchGroupCountAndFieldNameSize(regexRecordToMatch, matcher)

					final T importedRecordInstance = getClazzInstance(regexRecordToMatch)
					importedRecordsOfCurrent.add(importedRecordInstance)

					//todo try to remove that cast
					propertiesSetter.setAllProperties(importedRecordInstance, matcher, importedRecordsByName as Map<RegexRecordToMatch, List<Object>>)
					if (regexRecordToMatch.calculatedFields) {
						regexRecordToMatch.calculatedFields.call(importedRecordInstance)
					}
				}
			}
		}

		final List<T> importedRecordsList = createImportedRecordsList(importedRecordsByName)
		return importedRecordsList
	}

	private Map<RegexRecordToMatch, List<T>> getListOfRecortsToImportation(List<RegexRecordToMatch> regexRecordToMatchList) {
		final Map<RegexRecordToMatch, List<T>> importedRecordsByName = [:]

		regexRecordToMatchList.each { RegexRecordToMatch entry ->
			importedRecordsByName[entry] = []
		}

		return importedRecordsByName
	}

	private T getClazzInstance(final RegexRecordToMatch regexRecordToMatch) {
		final Class<T> clazzRegistro = regexRecordToMatch.clazzRegistroImportado
		final T registroImportado = clazzRegistro.newInstance()
		return registroImportado
	}

	private List<T> createImportedRecordsList(final Map<RegexRecordToMatch, List<T>> importedRecordsByName) {
		final List<T> importedRecordsList = []
		importedRecordsByName.each { RegexRecordToMatch entry, List<T> registrosImportados ->
			if (entry.returnImportedRecord) {
				importedRecordsList.addAll(registrosImportados)
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
