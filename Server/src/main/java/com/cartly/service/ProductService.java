package com.cartly.service;

import java.math.BigDecimal;
import java.util.List;

import com.cartly.dao.CategoryDAO;
import com.cartly.dao.ProductDAO;
import com.cartly.dto.ProductResponse;
import com.cartly.entity.Category;
import com.cartly.entity.Product;

public class ProductService {
	private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    
    public ProductService(ProductDAO productDAO, CategoryDAO categoryDAO) {
        this.productDAO = productDAO;
        this.categoryDAO = categoryDAO;
    }
    
    public ProductResponse createProduct(
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            String imageUrl,
            Long categoryId) {
    	
    	validateProduct(name, price, stock, categoryId);
    	
    	Category category = categoryDAO.findById(categoryId);
    	
    	if (category == null) {
            throw new IllegalArgumentException("Category not found");
        }
    	
    	Product product = new Product();
    	product.setName(name.trim());
    	product.setDescription(description);
    	product.setPrice(price);
    	product.setStock(stock);
    	product.setActive(true);
    	product.setImageUrl(imageUrl);
    	product.setCategory(category);
    	
    	Product saved = productDAO.save(product);
    	
    	return toResponse(saved);
    }
    
    public ProductResponse getProductById(Long id) {
    	if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
    	
    	Product product = productDAO.findById(id);
    	
    	if (product == null || !product.isActive()) {
            throw new IllegalArgumentException("Product not found");
        }
    	
    	return toResponse(product);
    }
    
    public List<ProductResponse> getAllProducts() {

        return productDAO.findAllActive()
                .stream()
                .map(this::toResponse)
                .toList();
    }
    
    public List<ProductResponse> getProductsByCategory(Long categoryId) {

        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }

        Category category = categoryDAO.findById(categoryId);

        if (category == null) {
            throw new IllegalArgumentException("Category not found");
        }

        return productDAO.findByCategory(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    
    public List<ProductResponse> searchProducts(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Search keyword is required");
        }

        return productDAO.searchByName(keyword.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse updateProduct(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            String imageUrl,
            Long categoryId) {
    	
    	if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
    	
    	validateProduct(name, price, stock, categoryId);
    	
    	Product product = productDAO.findById(id);
    	
    	if (product == null || !product.isActive()) {
            throw new IllegalArgumentException("Product not found");
        }
    	
    	Category category = categoryDAO.findById(categoryId);

        if (category == null) {
            throw new IllegalArgumentException("Category not found");
        }
        
        product.setName(name.trim());
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setImageUrl(imageUrl);
        product.setCategory(category);

        Product updated = productDAO.update(product);

        return toResponse(updated);
    }
    
    public void deactivateProduct(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }

        Product product = productDAO.findById(id);

        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        product.setActive(false);

        productDAO.update(product);
    }
    
    public void validateProduct(String name,
            BigDecimal price,
            Integer stock,
            Long categoryId) {
    	
    	if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Product name is required");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than or equal to zero");
        }

        if (stock == null || stock < 0) {
            throw new IllegalArgumentException(
                    "Stock must be greater than or equal to zero");
        }

        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException(
                    "Valid category ID is required");
        }
    }
    
    private ProductResponse toResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setImageUrl(product.getImageUrl());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        return response;
    }
}
