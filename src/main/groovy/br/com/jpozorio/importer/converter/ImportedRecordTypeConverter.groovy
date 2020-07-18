package br.com.jpozorio.importer

import groovy.transform.CompileStatic

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@CompileStatic
class ImportedRecordTypeConverter {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern('dd/MM/yyyy')

	static Object getTypedValue(
			final Object object,
			final String field,
			final String value
	) {
		final PropertyValue property = object.metaPropertyValues.find { PropertyValue pv -> pv.name == field }
		if (property) {
			final Class clazzType = property.type
			if (LocalDate.isAssignableFrom(clazzType)) {
				return LocalDate.parse(value, DATE_TIME_FORMATTER)
			} else if (BigDecimal.isAssignableFrom(clazzType)) {
				toBigDecimal(value)
			} else if (Boolean.isAssignableFrom(clazzType)) {
				return value.toBoolean()
			} else {
				return value
			}
		} else {
			return null
		}
	}

	private static BigDecimal toBigDecimal(String matcher) {
		return matcher.replace('.', '').replace(',', '.').toBigDecimal()
	}
}
