package br.com.jpozorio.importer.regex

import br.com.jpozorio.importer.ImportedRecord
import groovy.transform.CompileStatic

import java.util.regex.Pattern
import java.util.stream.Collectors

@CompileStatic
class RegexRecordToMatch<T extends ImportedRecord> {

	final Class<T> clazzRegistroImportado
	final String name
	final List<RegexRecordToMatch> parents
	final String regexHeader
	@SuppressWarnings('GrFinalVariableAccess')
	final Pattern patternHeader
	final List<String> regex
	final List<Pattern> patterns
	final List<String> fieldNames
	final Closure calculatedFields
	final Boolean returnImportedRecord

	RegexRecordToMatch(
			final String name,
			final String regex,
			final List<String> fieldNames,
			final Class<T> clazzRegistroImportado,
			final List<RegexRecordToMatch> parent,
			final Boolean returnImportedRecord = true,
			final String regexHeader = null,
			final Closure calculatedFields = null
	) {
		this(name, [regex], fieldNames, clazzRegistroImportado, parent, returnImportedRecord, regexHeader, calculatedFields)
	}

	RegexRecordToMatch(
			final String name,
			final List<String> regex,
			final List<String> fieldNames,
			final Class<T> clazzRegistroImportado,
			final List<RegexRecordToMatch> parent,
			final Boolean returnImportedRecord = true,
			final String regexHeader = null,
			final Closure calculatedFields = null
	) {
		this.name = name
		this.regex = regex
		this.fieldNames = fieldNames
		this.regexHeader = regexHeader
		this.clazzRegistroImportado = clazzRegistroImportado
		this.patterns = regex.stream().map({ final String r -> Pattern.compile(r) }).collect(Collectors.toList())
		this.parents = parent
		this.calculatedFields = calculatedFields
		this.returnImportedRecord = returnImportedRecord
		if (regexHeader) {
			this.patternHeader = Pattern.compile(regexHeader)
		}
	}
}
