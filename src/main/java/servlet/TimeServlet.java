package servlet;//  Завдання №1 - напиши сервлет, який віддає HTML
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

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(value = "/time")
public class Time extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=utf-8");

        resp.getWriter().write("Hello");
        resp.getWriter().close();
    }
}
