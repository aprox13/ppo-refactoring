package ru.akirakozov.sd.refactoring.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Html200ResponseEnricher {
    private static final String CONTENT_TYPE = "text/html";
    private static final int STATUS_CODE = HttpServletResponse.SC_OK;

    private boolean wrapToHtmlTagFlag;
    private final List<String> lines = new ArrayList<>();

    private Html200ResponseEnricher() {
    }

    public static Html200ResponseEnricher newResponseEnricher() {
        return new Html200ResponseEnricher();
    }

    public Html200ResponseEnricher wrapToHtmlTag(boolean wrap) {
        this.wrapToHtmlTagFlag = wrap;
        return this;
    }

    public Html200ResponseEnricher addLine(String line) {
        this.lines.add(line);
        return this;
    }

    public Html200ResponseEnricher addLines(List<String> lines) {
        this.lines.addAll(lines);
        return this;
    }

    public void enrich(HttpServletResponse response) throws IOException {
        response.setStatus(STATUS_CODE);
        response.setContentType(CONTENT_TYPE);


        String data = String.join("\n", lines);
        if (!lines.isEmpty()) {
            data += "\n";
        }

        if (wrapToHtmlTagFlag) {
            data = String.format("<html><body>\n%s</body></html>\n", data);
        }

        response.getWriter().print(data);
    }
}
