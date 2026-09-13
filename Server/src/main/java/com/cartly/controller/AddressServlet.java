package com.cartly.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import com.cartly.dao.AddressDAO;
import com.cartly.dao.UserDAO;
import com.cartly.dto.AddressRequest;
import com.cartly.dto.AddressResponse;
import com.cartly.entity.Address;
import com.cartly.service.AddressService;
import com.cartly.util.TransactionManagerFactory;

/**
 * Servlet implementation class AddressServlet
 */
@WebServlet("/api/addresses/*")
public class AddressServlet extends HttpServlet {
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	private final AddressService addressService =
            new AddressService(
                    new AddressDAO(),
                    new UserDAO(),
                    new TransactionManagerFactory()
            );

	@Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
		
		Long userId = getUserId(request, response);
		
		if (userId == null) {
            return;
        }
		
		String path = request.getPathInfo();
		
		try {
			if (path == null || path.equals("/")) {
				List<Address> addresses =
                        addressService.getUserAddresses(userId);

                List<AddressResponse> result =
                        addresses.stream()
                                .map(AddressResponse::new)
                                .toList();

                sendJson(response, HttpServletResponse.SC_OK, result);
			} else {

                Long addressId = parseId(path);

                Address address =
                        addressService.getAddress(userId, addressId);

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        new AddressResponse(address)
                );
            }
		} catch (IllegalArgumentException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
	}

	@Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = getUserId(request, response);

        if (userId == null) {
            return;
        }

        try {
            AddressRequest addressRequest =
                    objectMapper.readValue(
                            request.getReader(),
                            AddressRequest.class
                    );

            Address address = toEntity(addressRequest);

            Address savedAddress =
                    addressService.addAddress(userId, address);

            sendJson(
                    response,
                    HttpServletResponse.SC_CREATED,
                    new AddressResponse(savedAddress)
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }
	
	@Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = getUserId(request, response);

        if (userId == null) {
            return;
        }

        try {
            Long addressId =
                    parseId(request.getPathInfo());

            AddressRequest addressRequest =
                    objectMapper.readValue(
                            request.getReader(),
                            AddressRequest.class
                    );

            Address updatedAddress =
                    toEntity(addressRequest);

            addressService.updateAddress(
                    userId,
                    addressId,
                    updatedAddress
            );

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = getUserId(request, response);

        if (userId == null) {
            return;
        }

        try {
            Long addressId =
                    parseId(request.getPathInfo());

            addressService.deleteAddress(
                    userId,
                    addressId
            );

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    private Long getUserId(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required"
            );

            return null;
        }

        Object userId = session.getAttribute("userId");

        if (!(userId instanceof Long)) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid authentication session"
            );

            return null;
        }

        return (Long) userId;
    }

    private Long parseId(String path) {

        if (path == null ||
                path.equals("/") ||
                path.length() <= 1) {

            throw new IllegalArgumentException(
                    "Address ID is required"
            );
        }

        String idString = path.substring(1);

        try {
            return Long.parseLong(idString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid address ID"
            );
        }
    }

    private Address toEntity(AddressRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Address request cannot be null"
            );
        }

        Address address = new Address();

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());

        return address;
    }

    private void sendJson(
            HttpServletResponse response,
            int status,
            Object data)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                data
        );
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse(message)
        );
    }

    public static class ErrorResponse {

        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

}
