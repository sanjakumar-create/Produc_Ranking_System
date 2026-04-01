
# Relational Database Management Systems (RDBMS) Analysis

## 1. ACID Inconsistency (Consistency & Isolation)

### Part A: Bringing the Database to an Inconsistent State (The "C" in ACID)
**The Concept:** "Consistency" ensures that a database moves from one valid state to another. [cite_start]Every rule, foreign key, and constraint must be obeyed[cite: 290].

**The Scenario:** In our E-commerce system, a `Product` is strictly linked to its `ProductMetrics` via a One-To-One relationship. A metric cannot exist without a product.

**The Inconsistency:** Imagine an admin clicks "Delete Product" on the frontend. The Java backend fires two JDBC queries:
1. `DELETE FROM products WHERE id = 5;`
2. `DELETE FROM product_metrics WHERE product_id = 5;`

If the application crashes, the server loses power, or the database connection drops *exactly* after Query 1 but before Query 2, we have a massive problem. [cite_start]The product is gone, but the metrics remain[cite: 290, 291].

**Why this is bad:** We are left with "orphaned" metrics data. Not only does this waste hard drive space, but if a background job tries to calculate the average rating of all products by reading the metrics table, it will factor in ghost data, corrupting our analytics.

**The Fix (Atomicity):** Both queries must be wrapped in a database Transaction (e.g., using Spring's `@Transactional`). A transaction guarantees **Atomicity** ("All or Nothing"). If Query 2 fails, the database automatically undoes Query 1, ensuring the database remains perfectly Consistent.

### Part B: Isolation Levels and Concurrent Users (The "I" in ACID)
[cite_start]**The Concept:** "Isolation" dictates how the database behaves when 1,000 different users try to read and write the exact same data at the exact same millisecond[cite: 300].

[cite_start]**The Default Behavior:** By default, PostgreSQL operates at the `READ COMMITTED` isolation level[cite: 300]. This is a good balance of speed and safety. It ensures that a query only sees data that has been officially "committed" (saved) by other users, preventing us from reading half-finished updates (Dirty Reads).

**The Problem: "Non-Repeatable Reads"**
Imagine a scenario with two threads (users):
1. **Thread B** starts a transaction and queries: *"What is the ranking score of the iPhone?"* (Result: 100 points).
2. **Thread A** updates the iPhone's score to 150 points and hits COMMIT.
3. **Thread B** (still inside its original transaction) runs the exact same query again just to be sure. (Result: 150 points).

Even though Thread B never changed anything, the data magically changed right before its eyes. This is a **Non-Repeatable Read**.

[cite_start]**The Fix:** To prevent this, we must instruct the database to upgrade the isolation level to `REPEATABLE READ` or `SERIALIZABLE`[cite: 301].
* `REPEATABLE READ` takes a frozen "snapshot" of the database the moment Thread B starts its transaction. [cite_start]Even if Thread A commits changes, Thread B will confidently keep seeing the old 100 points until its transaction finishes[cite: 301].
* `SERIALIZABLE` is even stricter, forcing competing transactions to wait in a perfect single-file line, though it is much slower for performance.

---

## 2. Database Indexing (Single & Compound)

To see why indexes are critical for performance, we cannot test on 10 products. We must test on a massive scale. [cite_start]I generated 1,000,000 dummy products in the database using this script[cite: 303]:

```sql
INSERT INTO products (name, description, price, brand)
SELECT 'Product ' || i, 'Desc', random() * 2000, 'Brand ' || (i % 20)
FROM generate_series(1, 1000000) AS i;
```

### Step 1: Testing Without an Index
[cite_start]First, I ran a query to find all products from a specific brand under a certain price, using `EXPLAIN ANALYZE` to see exactly what the database engine is doing behind the scenes[cite: 304, 305].

`EXPLAIN ANALYZE SELECT * FROM products WHERE brand = 'Brand 5' AND price < 500;`

* **Execution Time:** 49.283 ms
* **The "Under the Hood" Observation:** The database query planner chose a **`Parallel Seq Scan`**. Because the database has no index to find 'Brand 5', it was forced to start at row 1 and read every single one of the 1,000,000 rows. Because the table is so large, PostgreSQL automatically launched 2 Background Workers to scan the table in parallel. Even with parallel processing, it had to manually remove over 329,112 rows per worker.

### Step 2: Testing With a Compound Index
[cite_start]I created a compound (multi-column) index on the exact two columns I was filtering by[cite: 307, 308]. An index creates a separate, highly optimized B-Tree data structure in memory.

`CREATE INDEX idx_brand_price ON products(brand, price);`

[cite_start]I ran the exact same query again[cite: 308].

* **Execution Time:** 16.977 ms
* **The "Under the Hood" Observation:** The execution time dropped by roughly 65%. The database stopped the Seq Scan and performed a **`Bitmap Index Scan`**. PostgreSQL read the index, generated an in-memory bitmap of the 12,724 matching rows, and then pulled them directly from the heap.

### Step 3: The Danger of Partial Columns in Compound Indexes
[cite_start]Finally, I ran a query using *only* the second column of the index (`price`)[cite: 308, 309].

`EXPLAIN ANALYZE SELECT * FROM products WHERE price < 500;`

* **Execution Time:** 80.501 ms
* **The "Under the Hood" Observation:** The query is slow again! Why? [cite_start]Because B-Tree compound indexes work exactly like a real-life Phone Book[cite: 309].
  If a phone book is sorted by `(Last Name, First Name)`, it is incredibly fast to find "Smith, John".
  However, if I ask you to find everyone named "John" (ignoring the Last Name), the phone book's sorting is useless to you. You have to read the entire book from page 1.

[cite_start]Because `price` is the *second* column in our compound index `(brand, price)`, the database could not efficiently use the index from left-to-right[cite: 309]. It reverted to a slower parallel sequential scan. **Conclusion:** The order of columns in a compound index is absolutely critical for query optimization.
```