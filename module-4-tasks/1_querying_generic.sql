-- 1. Product with the highest price (equivalent to biggest population)
SELECT * FROM products ORDER BY price DESC LIMIT 1;

-- 2. Products with a price higher than the average price (Subquery)
SELECT * FROM products
WHERE price > (SELECT AVG(price) FROM products);

-- 3. Product with the longest name
SELECT * FROM products
ORDER BY LENGTH(name) DESC LIMIT 1;

-- 4. All products with a name containing the letter "Pro", sorted alphabetically
SELECT * FROM products
WHERE name ILIKE '%Pro%' ORDER BY name ASC;

-- 5. Product with a price closest to the average price of all products
SELECT * FROM products
ORDER BY ABS(price - (SELECT AVG(price) FROM products)) ASC LIMIT 1;

-- 6. Count of products for each brand (Group By)
SELECT brand, COUNT(product_id) as total_products
FROM products GROUP BY brand;

-- 7. Average price per brand, sorted from most expensive brand to cheapest
SELECT brand, AVG(price) as avg_price
FROM products GROUP BY brand ORDER BY avg_price DESC;

-- 8. Find brands that have an average product price of less than $500 (HAVING clause)
SELECT brand FROM products
GROUP BY brand HAVING AVG(price) < 500;

-- 9. Find pairs of products that have the exact same price (Self Join)
SELECT p1.product_id, p2.product_id, p1.name, p1.price
FROM products p1
         JOIN products p2 ON p1.price = p2.price AND p1.product_id < p2.product_id;