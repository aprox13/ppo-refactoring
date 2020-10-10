package ru.akirakozov.sd.refactoring.common;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import javax.servlet.http.HttpServletResponse;

public class SuccessfulHtmlMatcher extends TypeSafeDiagnosingMatcher<HttpServletResponse> {
    private SuccessfulHtmlMatcher() {
    }

    @Override
    protected boolean matchesSafely(HttpServletResponse item, Description mismatchDescription) {
        mismatchDescription.appendText("was ").appendValue(String.format("%d - %s", item.getStatus(), item.getContentType()));
        return item.getStatus() == HttpServletResponse.SC_OK &&
                item.getContentType().equals("text/html");
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("A successful 200 - text/html");
    }

    public static SuccessfulHtmlMatcher isSuccessfulHtml() {
        return new SuccessfulHtmlMatcher();
    }
}