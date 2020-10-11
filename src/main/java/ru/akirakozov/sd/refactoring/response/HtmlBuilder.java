package ru.akirakozov.sd.refactoring.response;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HtmlBuilder {

    private boolean warpToHtmlTag = false;

    private final List<String> lines = new ArrayList<>();

    private HtmlBuilder() {
    }

    public static HtmlBuilder newBuilder() {
        return new HtmlBuilder();
    }

    public HtmlBuilder wrapToHtmlTag() {
        warpToHtmlTag = true;
        return this;
    }

    public HtmlBuilder addLine(String line) {
        this.lines.add(line);
        return this;
    }

    public HtmlBuilder addLines(List<String> lines) {
        this.lines.addAll(lines);
        return this;
    }

    public HtmlBuilder accept(Consumer<HtmlBuilder> consumer) {
        consumer.accept(this);
        return this;
    }

    public String build() {
        String data = String.join("\n", lines);
        if (!lines.isEmpty()) {
            data += "\n";
        }

        if (warpToHtmlTag) {
            data = String.format("<html><body>\n%s</body></html>\n", data);
        }

        return data;
    }
}
