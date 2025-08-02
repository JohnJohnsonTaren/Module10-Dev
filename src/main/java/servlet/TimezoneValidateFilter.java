package servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneId;

@WebFilter(value = "/time")
public class TimezoneValidateFilter extends HttpFilter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        String timezoneParam = req.getParameter("timezone");
        if (timezoneParam != null && !timezoneParam.isBlank()) {
            try {
                String processedTimezone = timezoneParam.trim().replace(" ", "+");
                ZoneId.of(processedTimezone);
                chain.doFilter(request, response);
            } catch (DateTimeException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("text/html; charset=UTF-8");
                resp.getWriter().write("<html><body><h1>Invalid timezone</h1></body></html>");
                resp.getWriter().close();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}