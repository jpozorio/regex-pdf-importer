package br.com.jpozorio.importer

import groovy.transform.CompileStatic

@CompileStatic
class ImportedRecordsList<T> {

	List<T> importedRecordsList
	int matchedHeaderLineMatched

	ImportedRecordsList(List<T> importedRecordsList) {
		this.importedRecordsList = importedRecordsList
		this.matchedHeaderLineMatched = -1
	}
}
