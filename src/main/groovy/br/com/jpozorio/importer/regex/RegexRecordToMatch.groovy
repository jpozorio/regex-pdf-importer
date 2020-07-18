package br.com.jpozorio.importer.regex

import br.com.jpozorio.importer.IImportedRecord
import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class RegexRecordToMatch<T extends IImportedRecord> {

	final Class<T> clazzRegistroImportado
	final String name
	final RegexRecordToMatch parent
	final String regexHeader
	@SuppressWarnings('GrFinalVariableAccess')
	final Pattern patternHeader
	final String regex
	final Pattern pattern
	final List<String> fieldNames
	final Closure calculatedFields
	final Boolean returnImportedRecord

	RegexRecordToMatch(
			final String name,
			final String regex,
			final List<String> fieldNames,
			final Class<T> clazzRegistroImportado,
			final RegexRecordToMatch parent,
			final Boolean returnImportedRecord = true,
			final String regexHeader = null,
			final Closure calculatedFields = null
	) {
		this.name = name
		this.regex = regex
		this.fieldNames = fieldNames
		this.regexHeader = regexHeader
		this.clazzRegistroImportado = clazzRegistroImportado
		this.pattern = Pattern.compile(regex)
		this.parent = parent
		this.calculatedFields = calculatedFields
		this.returnImportedRecord = returnImportedRecord
		if (regexHeader) {
			this.patternHeader = Pattern.compile(regexHeader)
		}
	}
}
