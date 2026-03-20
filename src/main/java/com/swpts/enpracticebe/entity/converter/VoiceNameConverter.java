package com.swpts.enpracticebe.entity.converter;

import com.swpts.enpracticebe.constant.VoiceName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VoiceNameConverter implements AttributeConverter<VoiceName, String> {

    @Override
    public String convertToDatabaseColumn(VoiceName voiceName) {
        return voiceName != null ? voiceName.getValue() : null;
    }

    @Override
    public VoiceName convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        for (VoiceName voiceName : VoiceName.values()) {
            if (voiceName.getValue().equals(dbData)) {
                return voiceName;
            }
        }
        throw new IllegalArgumentException("Unknown voice name: " + dbData);
    }
}
