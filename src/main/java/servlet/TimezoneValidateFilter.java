package servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneId;

@WebFilter(value = "/time")
public class TimezoneValidateFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) resp;

        String timezoneParam = httpRequest.getParameter("timezone");
        if (timezoneParam != null && !timezoneParam.isEmpty()) {
            String processedTimezone = timezoneParam.trim();
            if (processedTimezone.startsWith("UTC ") && processedTimezone.length() > 4
                    && Character.isDigit(processedTimezone.charAt(4))) {
                processedTimezone = "UTC+" + processedTimezone.substring(4);
            }

            boolean isValidTimezone = true;
            try {
                ZoneId.of(processedTimezone, ZoneId.SHORT_IDS);
            } catch (java.time.format.DateTimeParseException | java.time.zone.ZoneRulesException e) {
                isValidTimezone = false;
            }

            if (!isValidTimezone) {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.setContentType("text/html; charset=UTF-8");
                httpResponse.getWriter().println("<html><body><h1>Invalid timezone</h1></body></html>");
                return;
            }
        }
        chain.doFilter(req, resp);
    }
}
