package br.com.jpozorio.importer

import br.com.jpozorio.importer.regex.RegexRecordToMatch
import groovy.transform.CompileStatic

import java.util.regex.Matcher
import java.util.regex.Pattern

@CompileStatic
class PdfImporter<T> {

	final File file
	final String password

	PdfImporter(File file, String password = '') {
		this.file = file
		this.password = password ?: ''
	}

	List<T> readFile(final List<RegexRecordToMatch> regexRecordToMatchList) {
		final PdfToText pdfToText = new PdfToText(this.file, this.password)
		final Map<RegexRecordToMatch, ImportedRecordsList> importedRecordsByName = getListOfRecortsToImportation(regexRecordToMatchList)

		//todo find a intelligent way to do this replaces
		final String stringFromPdf = pdfToText.stringFromFile.replace(' ', ' ').replace('­', '-')
		int lineIndex = 0
		stringFromPdf.eachLine { final String line ->
			lineIndex++

			final String trimmedLine = line.trim()
			for (final RegexRecordToMatch regexRecordToMatch in regexRecordToMatchList) {
				final ImportedRecordsList recordsList = importedRecordsByName[regexRecordToMatch]
				final List<T> importedRecordsOfCurrent = recordsList.importedRecordsList
				final ImportedRecordPropertiesSetter propertiesSetter = new ImportedRecordPropertiesSetter(regexRecordToMatch)

				final List<Pattern> patterns = regexRecordToMatch.patterns
				boolean matchHeader = regexRecordToMatch.patternHeader == null || (regexRecordToMatch.patternHeader != null && recordsList.matchedHeaderLineMatched == lineIndex - 1)

				for (Pattern pattern in patterns) {
					final Matcher matcher = pattern.matcher(trimmedLine)
					final boolean matches = matcher.matches()
					if (matches && matchHeader) {
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

					//shoud break loop when first pattern matches
					if (matches) {
						break
					}
				}
			}
		}

		final List<T> importedRecordsList = createImportedRecordsList(importedRecordsByName)
		return importedRecordsList
	}

	private Map<RegexRecordToMatch, ImportedRecordsList> getListOfRecortsToImportation(final List<RegexRecordToMatch> regexRecordToMatchList) {
		final Set<RegexRecordToMatch> regexRecordToMatchSet = regexRecordToMatchList.toSet()

		addAllParents(regexRecordToMatchList, regexRecordToMatchSet)

		final Map<RegexRecordToMatch, ImportedRecordsList> importedRecordsByName = [:]

		regexRecordToMatchSet.each { RegexRecordToMatch entry ->
			importedRecordsByName[entry] = new ImportedRecordsList([])
		}

		return importedRecordsByName
	}

	private List<RegexRecordToMatch> addAllParents(final List<RegexRecordToMatch> regexRecordToMatchList, final Set<RegexRecordToMatch> regexRecordToMatchSet) {
		return regexRecordToMatchList.each { final RegexRecordToMatch rrtm ->
			if (rrtm.parents) {
				regexRecordToMatchSet.addAll(rrtm.parents)
				addAllParents(rrtm.parents, regexRecordToMatchSet)
			}
		}
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
