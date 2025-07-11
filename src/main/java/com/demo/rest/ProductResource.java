package com.demo.rest;

import jakarta.annotation.Resource;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
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
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> searchProducts(
            @QueryParam("productName") String productName,
            @QueryParam("categoryName") String categoryName,
            @QueryParam("brandName") String brandName,
            @QueryParam("page") @DefaultValue("1") int page) throws SQLException {
        
        List<Product> products = new ArrayList<>();
        int pageSize = 100;
        int offset = (page - 1) * pageSize;
        
        String sql = "SELECT " +
                    "p.id, p.name, p.description, " +
                    "c.name AS category_name, " +
                    "b.name AS brand_name, " +
                    "(SELECT AVG(id) FROM products WHERE id % 100 < 50) AS random_metric " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.id " +
                    "LEFT JOIN brands b ON p.brand_id = b.id " +
                    "WHERE " +
                    "(? = '' OR p.name LIKE CONCAT('%', ?, '%')) " +
                    "AND (? = '' OR p.description LIKE CONCAT('%', ?, '%')) " + 
                    "AND (? = '' OR c.name LIKE CONCAT('%', ?, '%')) " +
                    "AND (? = '' OR b.name LIKE CONCAT('%', ?, '%')) " +
                    "ORDER BY p.id * (SELECT COUNT(*)/1000 + 1 FROM products) " +
                    "LIMIT ? OFFSET ?";
        
        try (Connection conn = ds.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // パラメータの設定（既存の8つのパラメータ）
            String productSearch = "%" + (productName != null ? productName : "") + "%";
            String categorySearch = "%" + (categoryName != null ? categoryName : "") + "%";
            String brandSearch = "%" + (brandName != null ? brandName : "") + "%";

            pstmt.setString(1, productName != null ? productName : "");
            pstmt.setString(2, productSearch);
            pstmt.setString(3, productName != null ? productName : "");
            pstmt.setString(4, productSearch);
            pstmt.setString(5, categoryName != null ? categoryName : "");
            pstmt.setString(6, categorySearch);
            pstmt.setString(7, brandName != null ? brandName : "");
            pstmt.setString(8, brandSearch);
            
            // 新しいパラメータ：LIMIT と OFFSET
            pstmt.setInt(9, pageSize);
            pstmt.setInt(10, offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getLong("id"));
                    product.setName(rs.getString("name"));
                    product.setDescription(rs.getString("description"));
                    product.setCategoryName(rs.getString("category_name"));
                    product.setBrandName(rs.getString("brand_name"));
                    products.add(product);
                }
            }
        }
        
        return products;
    }

    // 検索結果の総数を返すエンドポイントを追加
    @GET
    @Path("/search/count")
    @Produces(MediaType.APPLICATION_JSON)
    public int searchProductsCount(
            @QueryParam("productName") String productName,
            @QueryParam("categoryName") String categoryName,
            @QueryParam("brandName") String brandName) throws SQLException {
        
        String sql = "SELECT COUNT(*) AS total " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.id " +
                    "LEFT JOIN brands b ON p.brand_id = b.id " +
                    "WHERE " +
                    "(? = '' OR p.name LIKE CONCAT('%', ?, '%')) " +
                    "AND (? = '' OR p.description LIKE CONCAT('%', ?, '%')) " + 
                    "AND (? = '' OR c.name LIKE CONCAT('%', ?, '%')) " +
                    "AND (? = '' OR b.name LIKE CONCAT('%', ?, '%'))";
        
        try (Connection conn = ds.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String productSearch = "%" + (productName != null ? productName : "") + "%";
            String categorySearch = "%" + (categoryName != null ? categoryName : "") + "%";
            String brandSearch = "%" + (brandName != null ? brandName : "") + "%";

            pstmt.setString(1, productName != null ? productName : "");
            pstmt.setString(2, productSearch);
            pstmt.setString(3, productName != null ? productName : "");
            pstmt.setString(4, productSearch);
            pstmt.setString(5, categoryName != null ? categoryName : "");
            pstmt.setString(6, categorySearch);
            pstmt.setString(7, brandName != null ? brandName : "");
            pstmt.setString(8, brandSearch);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        
        return 0;
    }

    
    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Category> getCategories() throws SQLException {
        // 既存のコードをそのまま維持
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
        // 既存のコードをそのまま維持
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
