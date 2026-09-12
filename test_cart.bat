@echo off
set BASE_URL=http://localhost:8084/Cartly/api
set COOKIE_FILE=cookie.txt

echo 1. Register User
curl.exe -s -c %COOKIE_FILE% -X POST %BASE_URL%/auth/register -H "Content-Type: application/json" -d "{\"name\":\"Test User\",\"email\":\"testcart@example.com\",\"password\":\"password123\"}"
echo.
echo.

echo 2. Login User
curl.exe -s -c %COOKIE_FILE% -b %COOKIE_FILE% -X POST %BASE_URL%/auth/login -H "Content-Type: application/json" -d "{\"email\":\"testcart@example.com\",\"password\":\"password123\"}"
echo.
echo.

echo 3. Create Category (Assuming category 1 doesn't exist, we try to create it)
curl.exe -s -c %COOKIE_FILE% -b %COOKIE_FILE% -X POST %BASE_URL%/categories -H "Content-Type: application/json" -d "{\"name\":\"TestCategory\",\"description\":\"Desc\"}"
echo.
echo.

echo 4. Create Product (Assuming product 1 doesn't exist, we try to create it)
curl.exe -s -c %COOKIE_FILE% -b %COOKIE_FILE% -X POST %BASE_URL%/products -H "Content-Type: application/json" -d "{\"name\":\"TestProduct\",\"price\":100.0,\"stock\":10,\"categoryId\":1}"
echo.
echo.

echo 5. Get Empty Cart
curl.exe -s -b %COOKIE_FILE% -X GET %BASE_URL%/cart
echo.
echo.

echo 6. Add Item to Cart (Product 1)
curl.exe -i -s -b %COOKIE_FILE% -X POST %BASE_URL%/cart/items -H "Content-Type: application/json" -d "{\"productId\":1,\"quantity\":2}"
echo.
echo.

echo 7. Get Cart After Add
curl.exe -s -b %COOKIE_FILE% -X GET %BASE_URL%/cart
echo.
echo.

echo 8. Update Item Quantity to 5
curl.exe -i -s -b %COOKIE_FILE% -X PUT %BASE_URL%/cart/items/1 -H "Content-Type: application/json" -d "{\"quantity\":5}"
echo.
echo.

echo 9. Get Cart After Update
curl.exe -s -b %COOKIE_FILE% -X GET %BASE_URL%/cart
echo.
echo.

echo 10. Remove Item
curl.exe -i -s -b %COOKIE_FILE% -X DELETE %BASE_URL%/cart/items/1
echo.
echo.

echo 11. Get Cart After Remove
curl.exe -s -b %COOKIE_FILE% -X GET %BASE_URL%/cart
echo.
echo.
