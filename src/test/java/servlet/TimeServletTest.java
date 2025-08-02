package servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings
class TimeServletTest {
    private TimeServlet servlet;
    
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        servlet = new TimeServlet();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testDoGetWithoutTimezone() throws IOException {
        // When
        servlet.doGet(request, response);

        // Then
        verify(response).setContentType("text/html; charset=UTF-8");
        String content = stringWriter.toString();
        assertTrue(content.contains("UTC"));
        assertTrue(content.matches(".*\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} UTC.*"));
    }

    @Test
    void testDoGetWithTimezone() throws IOException {
        // Given
        when(request.getParameter("timezone")).thenReturn("UTC+2");

        // When
        servlet.doGet(request, response);

        // Then
        String content = stringWriter.toString();
        assertTrue(content.contains("UTC+2"),
                "Response should contain UTC+2 but was: " + content);
        assertTrue(content.matches(".*\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} UTC\\+2.*"),
                "DateTime format should match expected pattern");
    }

    @Test
    void testHtmlFormat() throws IOException {
        // When
        servlet.doGet(request, response);

        // Then
        String content = stringWriter.toString();
        assertTrue(content.startsWith("<html><body><h1>"));
        assertTrue(content.endsWith("</h1></body></html>"));
    }
}