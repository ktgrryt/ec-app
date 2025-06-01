    package com.demo;

    import jakarta.annotation.Resource;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.annotation.WebServlet;
    import jakarta.servlet.http.HttpServlet;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;

    import javax.sql.DataSource;
    import java.io.IOException;
    import java.sql.Connection;
    import java.sql.ResultSet;
    import java.sql.Statement;
    import java.sql.SQLException;

    @WebServlet("/products")
    public class DbServlet extends HttpServlet {

        @Resource(lookup = "jdbc/MySQLDS")
        private DataSource ds;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html; charset=UTF-8");
            try (Connection conn = ds.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, name, price FROM products")) {

                StringBuilder html = new StringBuilder("""

                    <!DOCTYPE html>
<html><head><meta charset='UTF-8'><title>商品一覧</title></head><body>

                    <h1>商品一覧</h1>
<table border='1'>
<tr><th>ID</th><th>Name</th><th>Price</th></tr>
""");
                while (rs.next()) {
                    html.append("<tr>")
                        .append("<td>").append(rs.getInt("id")).append("</td>")
                        .append("<td>").append(rs.getString("name")).append("</td>")
                        .append("<td>").append(rs.getBigDecimal("price")).append("</td>")
                        .append("</tr>");
                }
                html.append("</table></body></html>");
                resp.getWriter().print(html.toString());
            } catch (SQLException e) {
                throw new ServletException("Database query failed", e);
            }
        }
    }
