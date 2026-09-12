package com.cartly.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import com.cartly.dao.CartDAO;
import com.cartly.dao.CartItemDAO;
import com.cartly.dao.ProductDAO;
import com.cartly.dao.UserDAO;
import com.cartly.dto.CartItemRequest;
import com.cartly.dto.CartResponse;
import com.cartly.dto.UpdateCartItemRequest;
import com.cartly.service.CartService;
import com.cartly.util.HibernateTransactionManager;
import com.cartly.util.TransactionManagerFactory;

@WebServlet("/api/cart/*")
public class CartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
       
	private final CartService cartService =
	        new CartService(
	                new CartDAO(),
	                new CartItemDAO(),
	                new ProductDAO(),
	                new UserDAO(),
	                new TransactionManagerFactory()
	        );

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Long userId = getUserId(request);
		
		 CartResponse cartResponse =
	                cartService.getCart(userId);
		 
		 writeJson(response, HttpServletResponse.SC_OK, cartResponse);
	}


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		
		if (!"/items".equals(path)) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Endpoint not found");
            return;
        }
		
		Long userId = getUserId(request);
		
		CartItemRequest cartItemRequest =
                objectMapper.readValue(
                        request.getReader(),
                        CartItemRequest.class);
		
		cartService.addItem(
                userId,
                cartItemRequest.getProductId(),
                cartItemRequest.getQuantity()
        );
		
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}
	
	@Override
	protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
		
		String path = request.getPathInfo();

        if (path == null || !path.startsWith("/items/")) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Endpoint not found");
            return;
        }
        
        Long productId = parseProductId(
                path.substring("/items/".length()));
        
        Long userId = getUserId(request);
        
        UpdateCartItemRequest updateRequest =
                objectMapper.readValue(
                        request.getReader(),
                        UpdateCartItemRequest.class);
        
        cartService.updateItemQuantity(
                userId,
                productId,
                updateRequest.getQuantity()
        );

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		
	}
	
	@Override
	protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
		
		String path = request.getPathInfo();

        Long userId = getUserId(request);
        
        if ("/".equals(path) || path == null) {

            cartService.clearCart(userId);

        } else if (path.startsWith("/items/")) {

            Long productId = parseProductId(
                    path.substring("/items/".length()));

            cartService.removeItem(
                    userId,
                    productId);

        } else {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Endpoint not found");
            return;
        }

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}
	
	private Long getUserId(HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            throw new IllegalStateException(
                    "User is not authenticated");
        }

        Object userId =
                session.getAttribute("userId");

        if (!(userId instanceof Long)) {
            throw new IllegalStateException(
                    "User is not authenticated");
        }

        return (Long) userId;
    }
	
	private Long parseProductId(String value) {

        try {
            return Long.valueOf(value);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid product ID");
        }
    }
	
	private void writeJson(
            HttpServletResponse response,
            int status,
            Object data)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                data);
    }
}
