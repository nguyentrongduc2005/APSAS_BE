package com.project.apsas.service.impl;

import lombok.RequiredArgsConstructor;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.mapstruct.Named;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkdownService  {
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    @Named("markdownToHtml") // <-- Đặt tên cho phương thức này
    public String markdownToHtml(String md) {
        if (md == null) {
            return null;
        }
        var document = markdownParser.parse(md);
        return htmlRenderer.render(document);
    }
}
