package servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.ZoneId;
import java.util.TimeZone;

@WebFilter(value = "/time")
public class TimezoneValidateFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Ініціалізація фільтра (немає спеціальної логіки)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String timezoneParam = httpRequest.getParameter("timezone");

        // Перевірка наявності параметра timezone та його валідація
        if (timezoneParam != null && !timezoneParam.trim().isEmpty()) {
            String processedTimezone = timezoneParam.trim();

            // Обробка формату "UTC " для сумісності з TimeServlet, якщо потрібно
            // Хоча TimeZone.getTimeZone() може обробляти "GMT+X" або "GMT-X"
            if (processedTimezone.startsWith("UTC ") && processedTimezone.length() > 4
                    && Character.isDigit(processedTimezone.charAt(4))) {
                // Перетворюємо "UTC +X" або "UTC -X" на "GMT+X" або "GMT-X" для TimeZone.getTimeZone()
                // TimeZone.getTimeZone() краще працює з "GMT" префіксом
                processedTimezone = "GMT" + processedTimezone.substring(3);
            } else if (processedTimezone.startsWith("UTC") && processedTimezone.length() > 3 &&
                    (processedTimezone.charAt(3) == '+' || processedTimezone.charAt(3) == '-')) {
                // Якщо вже "UTC+X" або "UTC-X", просто замінюємо на "GMT"
                processedTimezone = "GMT" + processedTimezone.substring(3);
            }


            TimeZone tz = TimeZone.getTimeZone(processedTimezone);

            // TimeZone.getTimeZone() повертає "GMT" для нерозпізнаних ідентифікаторів.
            // Якщо оригінальний параметр не був "GMT" і отриманий ID є "GMT", це означає, що він недійсний.
            // Однак, якщо користувач ввів "GMT", то це буде валідним.
            // Більш надійний спосіб перевірити, чи ZoneId.of() може його розпізнати.
            // Але оскільки запит на використання TimeZone, ми будемо використовувати його логіку.
            // Щоб уникнути помилкової валідації "GMT" як недійсного, якщо це був оригінальний запит,
            // ми перевіряємо, чи отриманий ID відрізняється від оригінального, якщо оригінальний не "GMT".
            // Або, простіше, перевіряємо, чи отриманий ID "GMT" і чи оригінальний не був "GMT".

            // Більш надійний спосіб валідації часового поясу, використовуючи ZoneId.of(),
            // який кидає ZoneRulesException для недійсних ID.
            boolean isValidTimezone = true;
            try {
                // Спробуємо створити ZoneId, щоб перевірити, чи є часовий пояс дійсним
                // Цей підхід є більш надійним, ніж TimeZone.getTimeZone()
                String zoneIdToCheck = timezoneParam.trim();
                if (zoneIdToCheck.startsWith("UTC ")) {
                    zoneIdToCheck = "UTC" + zoneIdToCheck.substring(3);
                }
                ZoneId.of(zoneIdToCheck);
            } catch (Exception e) {
                isValidTimezone = false;
            }

            if (!isValidTimezone) {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST); // HTTP 400
                httpResponse.setContentType("text/html; charset=utf-8");
                httpResponse.getWriter().write("Invalid timezone");
                httpResponse.getWriter().close();
                return; // Зупиняємо подальшу обробку запиту
            }
        }

        // Продовжуємо ланцюг фільтрів, якщо часовий пояс дійсний або відсутній
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Знищення фільтра (немає спеціальної логіки)
    }
}
