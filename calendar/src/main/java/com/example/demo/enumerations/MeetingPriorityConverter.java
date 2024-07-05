package com.example.demo.enumerations;

import java.util.stream.Stream;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MeetingPriorityConverter implements AttributeConverter<MeetingPriority, Integer> {
 
    @Override
    public Integer convertToDatabaseColumn(MeetingPriority priority) {
        if (priority == null) {
            return null;
        }
        return priority.getCode();
    }

    @Override
    public MeetingPriority convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return Stream.of(MeetingPriority.values())
          .filter(m -> m.getCode().equals(code))
          .findFirst()
          .orElseThrow(IllegalArgumentException::new);
    }
}