package com.abdulin.nutritionapp.data.dto.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement

object StringListOrEmptySerializer : KSerializer<List<String>> {
    private val delegate = ListSerializer(String.serializer())

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: List<String>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        val jsonDecoder = decoder as? JsonDecoder ?: return delegate.deserialize(decoder)
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) {
            return emptyList()
        }
        return jsonDecoder.json.decodeFromJsonElement(delegate, element)
    }
}

object LongListOrEmptySerializer : KSerializer<List<Long>> {
    private val delegate = ListSerializer(Long.serializer())

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: List<Long>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<Long> {
        val jsonDecoder = decoder as? JsonDecoder ?: return delegate.deserialize(decoder)
        val element: JsonElement = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) {
            return emptyList()
        }
        return jsonDecoder.json.decodeFromJsonElement<List<Long>>(element)
    }
}
