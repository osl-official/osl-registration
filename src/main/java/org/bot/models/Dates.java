package org.bot.models;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public record Dates(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {}
