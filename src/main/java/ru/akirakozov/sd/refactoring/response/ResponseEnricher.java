package ru.akirakozov.sd.refactoring.response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseEnricher {
    public static final String HTML_CONTENT_TYPE = "text/html";
    public static final int STATUS_CODE_200 = HttpServletResponse.SC_OK;
    private int code;
    private String contentType;
    private HtmlBuilder body;

    public static ResponseEnricher newResponseEnricher() {
        return new ResponseEnricher();
    }

    public ResponseEnricher withCode(int code) {
        this.code = code;
        return this;
    }

    public ResponseEnricher withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public ResponseEnricher withBody(HtmlBuilder body) {
        this.body = body;
        return this;
    }

    public void enrich(HttpServletResponse response) throws IOException {
        response.setStatus(code);
        response.setContentType(contentType);

        response.getWriter().print(body.build());
    }
}
