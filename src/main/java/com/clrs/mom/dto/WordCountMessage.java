package com.clrs.mom.dto;

import java.util.Map;

public record WordCountMessage(
        int lineNumber,
        Map<String, Integer> counts
) {}