package com.clrs.mom.dto;

import java.util.List;

public record LineMessage(
        int lineNumber,
        String content,
        List<String> keywords
) {}
