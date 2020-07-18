package br.com.jpozorio.importador

import br.com.jpozorio.importador.regex.RegexRecordToMatch
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
			final Map<RegexRecordToMatch, List<Object>> importedRecordsByName
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
			final Map<RegexRecordToMatch, List<Object>> importedRecordsByName
	) {
		final RegexRecordToMatch parent = regexRecordToMatch.parent
		if (parent) {
			List<Object> importedRecordsOfParent = importedRecordsByName[parent]
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
	}
}
