package servlet;
//  Завдання №1 - напиши сервлет, який віддає HTML
//                  сторінку з поточним часом по UTC
//  Напиши сервлет TimeServlet.
//  Він має обробляти GET запит за адресою /time і віддавати
//  HTML сторінку з поточним часом по часовому поясу UTC.
//
//  Сторінка має виводити час (з точністю до секунд) та часовий пояс.
//  Наприклад, 2022-01-05 12:05:01 UTC
//
//  Оскільки це GET запит, то його можна протестувати в браузері.
//  Запусти програму, і переконайсь, що вона коректно працює і відкривається у браузері.
//  Ти маєш ввести адресу на кшталт http://localhost:8080/time в браузері, і отримати результат.

//Завдання №2 - розшир сервлет, щоб він приймав часовий пояс
//        Розшир сервлет з попереднього завдання, щоб він приймав один query параметр timezone, і повертав час у переданому часовому поясі.
//
//        Наприклад, якщо відкрити в браузері URL виду http://localhost:8080/time?timezone=UTC+2, то отримаємо результат на кшталт - 2022-01-05 12:05:01 UTC+2.
//
//        Якщо не передавати параметр timezone, то має повертатись час по UTC.

import jakarta.servlet.ServletException;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=utf-8");

        String timezoneParam = req.getParameter("timezone");
        ZoneId zoneId;

        if (timezoneParam != null && !timezoneParam.trim().isEmpty()) {
            String processedTimezone = timezoneParam.trim();

            if (processedTimezone.startsWith("UTC ") && processedTimezone.length() > 4
                    && Character.isDigit(processedTimezone.charAt(4))) {
                processedTimezone = "UTC+" + processedTimezone.substring(4);
            }
            zoneId = ZoneId.of(processedTimezone);
        } else {
            zoneId = ZoneId.of("UTC");
        }

        ZonedDateTime currentTimeInZone = ZonedDateTime.now(zoneId);

        String currentFormatTime = currentTimeInZone.format(DateTimeFormatter.ofPattern(
                "Дата: yyyy-MM-dd, Час: HH:mm:ss, z"
        ));

        resp.getWriter().write(currentFormatTime);

        resp.getWriter().close();
    }
}

