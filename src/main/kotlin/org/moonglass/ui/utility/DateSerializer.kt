package org.moonglass.ui.utility

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.Date

object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeDouble(value.getTime())
    }

    override fun deserialize(decoder: Decoder): Date {
        return Date(decoder.decodeDouble())
    }
}
