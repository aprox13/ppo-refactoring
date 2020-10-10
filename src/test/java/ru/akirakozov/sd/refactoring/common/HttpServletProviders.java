package ru.akirakozov.sd.refactoring.common;

import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import static org.mockito.Mockito.lenient;

public class HttpServletProviders {

    private static final String GET = "GET";

    public static HttpServletRequest provideGetRequestWithParams(Map<String, String> params) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn(GET).when(request).getMethod();
        lenient().when(request.getParameter(Mockito.anyString()))
                .thenAnswer((Answer<String>) invocationOnMock -> params.getOrDefault(invocationOnMock.<String>getArgument(0), null));
        return request;
    }

    public static HttpServletResponse provideResponse(Writer writer) {
        try {
            return new HttpServletResponseWrapper(getMockResponse(writer));
        } catch (IOException e) {
            Assert.fail("Couldn't provide response");
            return null;
        }
    }

    private static HttpServletResponse getMockResponse(Writer bodyWriter) throws IOException {
        PrintWriter writer = new PrintWriter(bodyWriter);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        lenient().doAnswer(invocationOnMock -> {
            lenient().doReturn(invocationOnMock.<String>getArgument(0)).when(response).getContentType();
            return null;
        }).when(response).setContentType(Mockito.anyString());

        lenient().doAnswer(invocationOnMock -> {
            lenient().doReturn(invocationOnMock.<Integer>getArgument(0)).when(response).getStatus();
            return null;
        }).when(response).setStatus(Mockito.anyInt());

        lenient().doReturn(writer).when(response).getWriter();

        return response;
    }


}
