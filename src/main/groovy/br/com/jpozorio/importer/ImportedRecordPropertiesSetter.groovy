package br.com.jpozorio.importer

import br.com.jpozorio.importer.converter.ImportedRecordTypeConverter
import br.com.jpozorio.importer.regex.RegexRecordToMatch
import groovy.transform.CompileStatic

import java.util.regex.Matcher

@CompileStatic
class ImportedRecordPropertiesSetter {

	final RegexRecordToMatch regexRecordToMatch

	ImportedRecordPropertiesSetter(final RegexRecordToMatch regexRecordToMatch) {
		this.regexRecordToMatch = regexRecordToMatch
	}

	void setAllProperties(
			final Object importedRecord,
			final Matcher matcher,
			final Map<RegexRecordToMatch, ImportedRecordsList> importedRecordsByName
	) {
		this.setPropertiesBasedOnFieldNames(importedRecord, matcher)
		this.copyParentFieldsToImportedRecord(importedRecord, importedRecordsByName)
	}

	private void setPropertiesBasedOnFieldNames(
			final Object importedRecord,
			final Matcher matcher
	) {
		regexRecordToMatch.fieldNames.eachWithIndex { final String field, final int idx ->
			final String value = matcher.group(idx + 1)
			if (importedRecord.hasProperty(field)) {
				final String methodName = "set${field.capitalize()}".toString()
				final Object typedValue = ImportedRecordTypeConverter.getTypedValue(importedRecord, field, value)
				importedRecord.invokeMethod(methodName, typedValue)
			}
		}
	}

	private void copyParentFieldsToImportedRecord(
			final Object importedRecord,
			final Map<RegexRecordToMatch, ImportedRecordsList> importedRecordsByName
	) {
		final List<RegexRecordToMatch> parents = regexRecordToMatch.parents

		passThroughGlobalVariable(parents, importedRecordsByName, importedRecord)
	}

	private void passThroughGlobalVariable(
			final List<RegexRecordToMatch> parents,
			final Map<RegexRecordToMatch, ImportedRecordsList> importedRecordsByName,
			final Object importedRecord
	) {
		if (!parents) {
			return
		}

		for (RegexRecordToMatch parent in parents) {
			if (parent) {
				final ImportedRecordsList parentImportedRecord = importedRecordsByName[parent]
				if (!parentImportedRecord) {
					throw new IllegalStateException('Trying to get fields from parent record that does not exists')
				}
				final List<Object> importedRecordsOfParent = parentImportedRecord.importedRecordsList
				if (importedRecordsOfParent) {
					Object lastParent = importedRecordsOfParent.last()
					parent.fieldNames.each { final String field ->
						if (importedRecord.hasProperty(field)) {
							final String methodName = "set${field.capitalize()}".toString()
							importedRecord.invokeMethod(methodName, lastParent[field])
						}
					}
				}
			}

			passThroughGlobalVariable(parent.parents, importedRecordsByName, importedRecord)
		}
	}
}
