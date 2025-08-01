package servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TimeServletAndFilterTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private PrintWriter writer;
    private StringWriter stringWriter;
    private FilterChain filterChain;
    private TimeServlet timeServlet;
    private TimezoneValidateFilter timezoneValidateFilter;

    @BeforeEach
    public void setUp() throws IOException {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        timeServlet = new TimeServlet();
        timezoneValidateFilter = new TimezoneValidateFilter();
    }

    // --- Тести для TimeServlet ---

    @Test
    public void testTimeServlet_NoTimezoneParam_DefaultsToUTC() throws IOException, ServletException {
        // Mocking behavior for request.getParameter("timezone") to return null
        when(request.getParameter("timezone")).thenReturn(null);

        // Call the servlet's doGet method
        timeServlet.doGet(request, response);

        // Verify content type
        verify(response).setContentType("text/html; charset=utf-8");

        // Verify the output HTML contains UTC time
        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("UTC"));

        // Generate expected UTC time for comparison
        ZonedDateTime currentTimeInUTC = ZonedDateTime.now(ZoneId.of("UTC"));
        String expectedFormatTime = currentTimeInUTC.format(DateTimeFormatter.ofPattern(
                "Дата: yyyy-MM-dd, Час: HH:mm:ss, z"
        ));
        assertTrue(responseContent.contains(expectedFormatTime));
    }

    @Test
    public void testTimeServlet_WithValidTimezoneParam() throws IOException, ServletException {
        String timezone = "Europe/London";
        when(request.getParameter("timezone")).thenReturn(timezone);

        timeServlet.doGet(request, response);

        verify(response).setContentType("text/html; charset=utf-8");

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains(timezone));

        ZonedDateTime currentTimeInZone = ZonedDateTime.now(ZoneId.of(timezone));
        String expectedFormatTime = currentTimeInZone.format(DateTimeFormatter.ofPattern(
                "Дата: yyyy-MM-dd, Час: HH:mm:ss, z"
        ));
        assertTrue(responseContent.contains(expectedFormatTime));
    }

    @Test
    public void testTimeServlet_WithDifferentValidTimezoneParam() throws IOException, ServletException {
        String timezone = "America/New_York";
        when(request.getParameter("timezone")).thenReturn(timezone);

        timeServlet.doGet(request, response);

        verify(response).setContentType("text/html; charset=utf-8");

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains(timezone));

        ZonedDateTime currentTimeInZone = ZonedDateTime.now(ZoneId.of(timezone));
        String expectedFormatTime = currentTimeInZone.format(DateTimeFormatter.ofPattern(
                "Дата: yyyy-MM-dd, Час: HH:mm:ss, z"
        ));
        assertTrue(responseContent.contains(expectedFormatTime));
    }

    @Test
    public void testTimeServlet_BlankTimezoneParam_DefaultsToUTC() throws IOException, ServletException {
        // This test simulates the filter passing a blank timezone parameter
        // The servlet itself will treat blank as null and default to UTC.
        when(request.getParameter("timezone")).thenReturn("   ");

        timeServlet.doGet(request, response);

        verify(response).setContentType("text/html; charset=utf-8");

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("UTC"));

        ZonedDateTime currentTimeInUTC = ZonedDateTime.now(ZoneId.of("UTC"));
        String expectedFormatTime = currentTimeInUTC.format(DateTimeFormatter.ofPattern(
                "Дата: yyyy-MM-dd, Час: HH:mm:ss, z"
        ));
        assertTrue(responseContent.contains(expectedFormatTime));
    }


    // --- Тести для TimezoneValidateFilter ---

    @Test
    public void testFilter_ValidTimezone_PassesToChain() throws IOException, ServletException {
        when(request.getParameter("timezone")).thenReturn("Europe/Kyiv");

        timezoneValidateFilter.doFilter(request, response, filterChain);

        // Verify that the filterChain's doFilter method was called,
        // meaning the request was passed on.
        verify(filterChain).doFilter(request, response);
        // Ensure no error status was set
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).getWriter(); // Filter should not write if it passes
    }

    @Test
    public void testFilter_InvalidTimezone_ReturnsBadRequest() throws IOException, ServletException {
        when(request.getParameter("timezone")).thenReturn("Invalid/Zone");

        timezoneValidateFilter.doFilter(request, response, filterChain);

        // Verify that the response status was set to 400 Bad Request
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        // Verify that content type was set
        verify(response).setContentType("text/html; charset=UTF-8");
        // Verify that the filter wrote an error message
        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("<h1>Invalid timezone</h1>"));
        // Verify that the filterChain's doFilter method was NOT called
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testFilter_NoTimezoneParam_PassesToChain() throws IOException, ServletException {
        when(request.getParameter("timezone")).thenReturn(null);

        timezoneValidateFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).getWriter();
    }

    @Test
    public void testFilter_BlankTimezoneParam_ReturnsBadRequest() throws IOException, ServletException {
        // According to the filter's current logic, a blank string is not a valid ZoneId
        when(request.getParameter("timezone")).thenReturn("   ");

        timezoneValidateFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).setContentType("text/html; charset=UTF-8");
        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("<h1>Invalid timezone</h1>"));
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testFilter_EmptyTimezoneParam_ReturnsBadRequest() throws IOException, ServletException {
        when(request.getParameter("timezone")).thenReturn("");

        timezoneValidateFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).setContentType("text/html; charset=UTF-8");
        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("<h1>Invalid timezone</h1>"));
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    // --- Інтеграційні тести (Filter -> Servlet) ---

    @Test
    public void testIntegration_ValidTimezone_RequestGoesThrough() throws IOException, ServletException {
        String timezone = "Asia/Tokyo";
        when(request.getParameter("timezone")).thenReturn(timezone);

        // Create an ArgumentCaptor to capture the actual HttpServletRequest and HttpServletResponse passed to the servlet
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        ArgumentCaptor<HttpServletResponse> responseCaptor = ArgumentCaptor.forClass(HttpServletResponse.class);

        // Configure the filterChain to call the servlet's doGet method
        doAnswer(invocation -> {
            // Simulate the filter chain calling the servlet
            timeServlet.doGet(requestCaptor.getValue(), responseCaptor.getValue());
            return null;
        }).when(filterChain).doFilter(requestCaptor.capture(), responseCaptor.capture());

        // Call the filter
        timezoneValidateFilter.doFilter(request, response, filterChain);

        // Verify filter passed it on
        verify(filterChain).doFilter(request, response); // Verify it was passed with the original objects
        verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST); // Ensure filter didn't set error

        // Verify servlet behavior
        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains(timezone));

        ZonedDateTime currentTimeInZone = ZonedDateTime.now(ZoneId.of(timezone));
        String expectedFormatTime = currentTimeInZone.format(DateTimeFormatter.ofPattern(
                "Дата: yyyy-MM-dd, Час: HH:mm:ss, z"
        ));
        assertTrue(responseContent.contains(expectedFormatTime));
    }

    @Test
    public void testIntegration_InvalidTimezone_FilterBlocksRequest() throws IOException, ServletException {
        when(request.getParameter("timezone")).thenReturn("Bad/Zone");

        // Call the filter
        timezoneValidateFilter.doFilter(request, response, filterChain);

        // Verify filter blocked the request
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).setContentType("text/html; charset=UTF-8");
        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("<h1>Invalid timezone</h1>"));

        // Verify servlet was NEVER called
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        // You could also verify that timeServlet.doGet was never called directly, but verifying filterChain is enough
    }

    @Test
    public void testIntegration_NoTimezoneParam_RequestGoesThroughAndDefaultsToUTC() throws IOException, ServletException {
        when(request.getParameter("timezone")).thenReturn(null);

        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        ArgumentCaptor<HttpServletResponse> responseCaptor = ArgumentCaptor.forClass(HttpServletResponse.class);

        doAnswer(invocation -> {
            timeServlet.doGet(requestCaptor.getValue(), responseCaptor.getValue());
            return null;
        }).when(filterChain).doFilter(requestCaptor.capture(), responseCaptor.capture());

        timezoneValidateFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("UTC"));

        ZonedDateTime currentTimeInUTC = ZonedDateTime.now(ZoneId.of("UTC"));
        String expectedFormatTime = currentTimeInUTC.format(DateTimeFormatter.ofPattern(
                "Дата: yyyy-MM-dd, Час: HH:mm:ss, z"
        ));
        assertTrue(responseContent.contains(expectedFormatTime));
    }
}