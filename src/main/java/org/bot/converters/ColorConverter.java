package org.bot.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.awt.*;

@Converter(autoApply = true)
public class ColorConverter implements AttributeConverter<Color, String> {

    @Override
    public String convertToDatabaseColumn(Color color) {
        return color != null ? String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()) : null;
    }

    @Override
    public Color convertToEntityAttribute(String dbData) {
        return dbData != null ? Color.decode(dbData) : null;
    }
}