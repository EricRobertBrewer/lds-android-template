package org.jdc.template.json

import com.google.gson.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Type

class LocalDateTypeConverter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    companion object {
        val FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_DATE
    }

    override fun serialize(src: LocalDate, srcType: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(FORMATTER.format(src))
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): LocalDate {
        return LocalDate.parse(json.asString, FORMATTER)
    }
}
