package com.demo.rest;

import jakarta.annotation.Resource;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("/api")
public class ProductResource {

    @Resource(lookup = "jdbc/MySQLDS")
    private DataSource ds;

    @GET
    @Path("/products")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> getProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        
        String sql = "SELECT p.id, p.name, p.description, p.category_id, p.brand_id, " +
                     "c.name as category_name, b.name as brand_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN brands b ON p.brand_id = b.id";
        
        try (Connection conn = ds.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getLong("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setCategoryId(rs.getLong("category_id"));
                product.setBrandId(rs.getLong("brand_id"));
                product.setCategoryName(rs.getString("category_name"));
                product.setBrandName(rs.getString("brand_name"));
                products.add(product);
            }
        }
        
        return products;
    }
    
    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Category> getCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        
        try (Connection conn = ds.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM categories")) {
            
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                categories.add(category);
            }
        }
        
        return categories;
    }
    
    @GET
    @Path("/brands")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Brand> getBrands() throws SQLException {
        List<Brand> brands = new ArrayList<>();
        
        try (Connection conn = ds.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM brands")) {
            
            while (rs.next()) {
                Brand brand = new Brand();
                brand.setId(rs.getLong("id"));
                brand.setName(rs.getString("name"));
                brands.add(brand);
            }
        }
        
        return brands;
    }
}
