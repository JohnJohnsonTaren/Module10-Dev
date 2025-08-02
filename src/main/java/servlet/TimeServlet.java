package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");

        String timezone = req.getParameter("timezone");
        ZoneId zoneId = timezone != null && !timezone.isBlank()
                ? ZoneId.of(timezone.trim().replace(" ", "+"))
                : ZoneId.of("UTC");

        ZonedDateTime currentTimeInZone = ZonedDateTime.now(zoneId);

        String zoneString = timezone != null && !timezone.isBlank() 
                ? timezone.trim().replace(" ", "+")
                : "UTC";
                
        String currentFormatTime = currentTimeInZone.format(DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss"
        )) + " " + zoneString;

        resp.getWriter().write("<html><body><h1>" + currentFormatTime + "</h1></body></html>");
    }
}