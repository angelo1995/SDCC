package com.example.demo.enumerations;

import java.util.stream.Stream;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MeetingStatusConverter implements AttributeConverter<MeetingStatus, Integer> {
 
    @Override
    public Integer convertToDatabaseColumn(MeetingStatus status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    public MeetingStatus convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return Stream.of(MeetingStatus.values())
          .filter(m -> m.getCode().equals(code))
          .findFirst()
          .orElseThrow(IllegalArgumentException::new);
    }
}
