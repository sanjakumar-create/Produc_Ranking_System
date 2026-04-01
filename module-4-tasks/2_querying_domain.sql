-- 1. Full CRUD set of queries for your domain entity (Products)
-- CREATE
INSERT INTO products (name, description, price, brand) VALUES ('Test Laptop', 'A fast laptop', 999.99, 'TestBrand');
-- READ
SELECT * FROM products WHERE name = 'Test Laptop';
-- UPDATE
UPDATE products SET price = 899.99 WHERE name = 'Test Laptop';
-- DELETE
DELETE FROM products WHERE name = 'Test Laptop';

-- 2. Search query with dynamic filters, pagination and sorting
-- (Use case: User searches for Apple products under $1500, views page 1, max 5 items)
SELECT * FROM products
WHERE brand = 'Apple' AND price < 1500
ORDER BY price ASC LIMIT 5 OFFSET 0;

-- 3. Search query with joined data for your use-cases
-- (Use case: Fetching a product's details alongside all its user reviews)
SELECT p.name, p.price, r.rating, r.comment
FROM products p
         JOIN reviews r ON p.product_id = r.product_id
WHERE p.product_id = 1;

-- 4. Statistic query
-- (Use case: Return brands and the average rating of their products to see who is the best manufacturer)
SELECT p.brand, ROUND(AVG(m.rating_average), 2) as overall_brand_rating, SUM(m.sales_count) as total_brand_sales
FROM products p
         JOIN product_metrics m ON p.product_id = m.product_id
GROUP BY p.brand;

-- 5. Top-something query
-- (Use case: Return products ordered by their trending score to populate the /trending endpoint)
SELECT p.name, p.brand, m.trending_score, m.view_count
FROM products p
         JOIN product_metrics m ON p.product_id = m.product_id
ORDER BY m.trending_score DESC LIMIT 10;