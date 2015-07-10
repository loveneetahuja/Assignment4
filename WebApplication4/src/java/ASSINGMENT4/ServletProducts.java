/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASSINGMENT4;

import com.mysql.jdbc.Connection;
import java.io.StringReader;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.stream.JsonParser;
//import javax.persistence.*;
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
import javax.ws.rs.*;
import org.json.simple.JSONArray;

/**
 *
 * @author Loveneet
 */
@Path("/product")
public class ServletProducts {

    @GET
    @Produces("application/json")
    public String doGet() {
        return getResults("SELECT * FROM product");
    }

    @GET
    @Produces("application/json")
    @Path("{product_id}")
    public String doGet(@PathParam("product_id") String product_id) {
        return getResults("SELECT * FROM product WHERE product_id = ?", product_id);
    }

    @POST
    @Consumes("application/json")
    public void doPost(String str) {
        JsonParser parser = Json.createParser(new StringReader(str));
        Map<String, String> mapKeyValue = new HashMap<>();
        String key = "", val;
        while (parser.hasNext()) {
            JsonParser.Event evt = parser.next();
            switch (evt) {
                case KEY_NAME:
                    key = parser.getString();
                    break;
                case VALUE_STRING:
                    val = parser.getString();
                    mapKeyValue.put(key, val);
                    break;
                case VALUE_NUMBER:
                    val = Integer.toString(parser.getInt());
                    mapKeyValue.put(key, val);
                    break;
            }
        }
        System.out.println(mapKeyValue);
        doPostOrPutOrDelete("INSERT INTO product (product_name,description, quantity) VALUES ( ?, ?, ?)",
                mapKeyValue.get("product_name"), mapKeyValue.get("description"), mapKeyValue.get("quantity"));
    }

    @PUT
    @Path("{product_id}")
    @Consumes("application/json")
    public void doPut(@PathParam("product_id") String id, String str) {
        JsonParser parser = Json.createParser(new StringReader(str));
        Map<String, String> mapKayValue = new HashMap<>();
        String key = "", val;
        while (parser.hasNext()) {
            JsonParser.Event evt = parser.next();
            switch (evt) {
                case KEY_NAME:
                    key = parser.getString();
                    break;
                case VALUE_STRING:
                    val = parser.getString();
                    mapKayValue.put(key, val);
                    break;
                case VALUE_NUMBER:
                    val = parser.getString();
                    mapKayValue.put(key, val);
                    break;
            }
        }
        System.out.println(mapKayValue);
        doPostOrPutOrDelete("UPDATE PRODUCT SET name = ?, description = ?, quantity = ? WHERE product_id = ?",
                mapKayValue.get("product_name"), mapKayValue.get("description"), mapKayValue.get("quantity"), id);

    }

    @DELETE
    @Path("{product_id}")
    public void doDelete(@PathParam("product_id") String id, String str) {
        doPostOrPutOrDelete("DELETE FROM product WHERE product_id = ?", id);
    }

    private void doPostOrPutOrDelete(String query, String... params) {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ServletProducts.class.getName()).log(Level.SEVERE, null, ex);
        }
        getResults("SELECT * FROM product");
    }

    private Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String jdbc = "jdbc:mysql://localhost/java";
            conn = (Connection) DriverManager.getConnection(jdbc, "root", "");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServletProducts.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }

    private String getResults(String query, String... params) {
        String result = new String();
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet rs = pstmt.executeQuery();
            JSONArray productArr = new JSONArray();
            while (rs.next()) {
                Map productMap = new LinkedHashMap();
                productMap.put("product_id", rs.getInt("product_id"));
                productMap.put("product_name", rs.getString("product_name"));
                productMap.put("description", rs.getString("description"));
                productMap.put("quantity", rs.getInt("quantity"));
                productArr.add(productMap);
            }
            result = productArr.toString();
        } catch (SQLException ex) {
            Logger.getLogger(ServletProducts.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result.replace("},", "},\n");
    }

}
