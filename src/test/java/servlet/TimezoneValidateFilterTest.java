package servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
class TimezoneValidateFilterTest {
    private TimezoneValidateFilter filter;
    
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        filter = new TimezoneValidateFilter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

    }

    @Test
    void testDoFilter_WithValidTimezone() throws ServletException, IOException {
        // Given
        when(request.getParameter("timezone")).thenReturn("UTC+2");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void testDoFilter_WithInvalidTimezone() throws ServletException, IOException {
        // Given
        when(request.getParameter("timezone")).thenReturn("    ");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void testDoFilter_WithNoTimezone() throws ServletException, IOException {
        // Given
        when(request.getParameter("timezone")).thenReturn(null);

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void testDoFilter_WithBlankTimezone() throws ServletException, IOException {
        // Given
        when(request.getParameter("timezone")).thenReturn("   ");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void testDoFilter_WithTimezoneContainingSpaces() throws ServletException, IOException {
        // Given
        when(request.getParameter("timezone")).thenReturn("UTC +2");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}