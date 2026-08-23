# Library Management System — LLD Notes

## 1. Problem Statement

Design a basic Library Management System for a physical library.

### Required functionality

- Admin can add books.
- Library can contain multiple bookshelves.
- Each bookshelf stores books using rows and columns.
- Each `Book` represents a concrete physical book.
- A book has an ID, name, author, and availability state.
- A book can be issued only when available.
- The system records the issue time.
- When a book is returned, the system:
  - finds the issued book,
  - calculates borrowing duration,
  - calculates the amount,
  - processes payment,
  - marks the book available again.

---

# 2. Classes I Designed

```text
Book
BookShelf
Library
Person
Admin
Customer
Payment
```

## Class relationship

```text
                         Person
                           ▲
                           │
                 ┌─────────┴─────────┐
                 │                   │
               Admin              Customer


                           Library
                              │
              ┌───────────────┼────────────────┐
              │               │                │
              ▼               ▼                ▼
        BookShelf List   Issued Books        Payment
              │          HashMap<Book,       │
              │             Long>            │
              ▼                              ▼
          Book[][]
              │
              ▼
             Book
```

---

# 3. `Book`

Each `Book` is a concrete physical book.

```text
Book
├── bookId
├── name
├── authorName
└── isAvailable
```

Main behavior:

```text
issueBook()
returnBook()
isAvailable()
```

The important design decision was that `Book` manages its own state.

```java
book.issueBook();
book.returnBook();
```

Instead of allowing outside classes to directly modify its availability.

---

# 4. `BookShelf`

A bookshelf physically stores books.

```text
BookShelf
├── row
├── column
└── Book[][]
```

Example:

```text
+-----------+-----------+-----------+
| Book A    | Book B    | Empty     |
+-----------+-----------+-----------+
| Book C    | Empty     | Book D    |
+-----------+-----------+-----------+
```

The shelf uses:

```java
Book[][] shelf;
```

Main responsibility:

```text
addBook()
store books
```

---

# 5. `Person`, `Admin`, and `Customer`

```text
              Person
                 ▲
                 │
          ┌──────┴──────┐
          │             │
        Admin        Customer
```

`Person` stores common information:

```text
name
phone number
```

`Admin` and `Customer` inherit from `Person`.

---

# 6. `Library`

The `Library` is the main coordinating class.

It contains:

```java
ArrayList<BookShelf> shelves;
HashMap<Book, Long> hm;
```

Meaning:

```text
shelves
    ↓
Multiple physical bookshelves


HashMap<Book, Long>

Book  → Issue Time
```

This was enough for the Version 1 requirements because the system needed to know:

```text
Which book is issued?
When was it issued?
```

---

# 7. Issuing a Book

The method:

```java
boolean issue(String bookName)
```

works like this:

```text
Library.issue(bookName)
        │
        ▼
Loop through all shelves
        │
        ▼
Loop through rows and columns
        │
        ▼
Find matching book
        │
        ▼
Is it available?
    ┌────┴────┐
   No        Yes
   │          │
Return      issueBook()
false         │
              ▼
       isAvailable = false
              │
              ▼
HashMap<Book, System.currentTimeMillis()>
```

---

# 8. Returning / Submitting a Book

The method:

```java
int submit(String bookName)
```

does:

```text
Find Book
    │
    ▼
Check that it is issued
    │
    ▼
Get issue time from HashMap
    │
    ▼
Get current time
    │
    ▼
totalTime = returnTime - issueTime
    │
    ▼
Convert time into days
    │
    ▼
Calculate amount
    │
    ▼
Process payment
    │
    ▼
book.returnBook()
    │
    ▼
Book becomes AVAILABLE
    │
    ▼
Remove book from HashMap
```

---

# 9. Complete Functional Flow

```text
Admin
  │
  ▼
Add Book
  │
  ▼
BookShelf
  │
  ▼
Book[][]


Customer requests Book
  │
  ▼
Library.issue()
  │
  ▼
Find available book
  │
  ▼
Book.issueBook()
  │
  ▼
Store issue time


Book Return
  │
  ▼
Library.submit()
  │
  ▼
Calculate duration
  │
  ▼
Calculate amount
  │
  ▼
Payment.processPayment()
  │
  ▼
Book.returnBook()
  │
  ▼
Remove active issue record
```

---

# 10. Data Structures Used

## `Book[][]`

Used to represent physical shelf positions.

## `ArrayList<BookShelf>`

Used because one library can have multiple bookshelves.

## `HashMap<Book, Long>`

Used to track active issued books.

```text
Key   → Book
Value → Issue Time
```

Example:

```text
Clean Code       → issue timestamp
Design Patterns  → issue timestamp
```

---

# 11. What the Initial Design Already Achieved

The implementation was fully functional.

It supported:

```text
✓ Add books
✓ Multiple bookshelves
✓ Store books physically
✓ Check availability
✓ Issue books
✓ Record issue time
✓ Return books
✓ Calculate borrowing duration
✓ Calculate amount
✓ Make book available again
✓ Payment processing
```

The design also demonstrated:

```text
Encapsulation
Inheritance
Composition
Collections
State management
```

Responsibilities:

```text
Book
→ manages availability

BookShelf
→ stores books

Library
→ coordinates operations

Person
→ common abstraction

Admin / Customer
→ different actors

Payment
→ payment processing
```

---

# 12. Natural Improvement: `Loan`

The original:

```java
HashMap<Book, Long>
```

is valid for Version 1.

However, if requirements grow and we need:

```text
Which customer borrowed the book?
Issue time?
Return time?
Payment details?
Loan status?
History?
```

then a dedicated `Loan` class becomes useful.

```text
Loan
├── Book
├── Customer
├── issueTime
├── returnTime
├── amount
└── status
```

The evolution is:

```text
VERSION 1

Book ──────► Issue Time


VERSION 2

Book + Customer + Issue/Return Information
                    │
                    ▼
                   Loan
```

The key point is that the original design was not wrong. `Loan` is a refactoring for future requirements.

---

# 13. Interview Explanation

A concise explanation of this design:

> I started with a simple implementation focused on the current requirements. A Book represents a concrete physical book and manages its own availability. BookShelf manages physical storage using a 2D array, while Library coordinates operations across multiple shelves.

> For the first version, I track active issued books using `HashMap<Book, Long>`, where the value is the issue time. This allows me to calculate the borrowing duration and amount.

> If the requirements expand to track customers, return history, loan status, or payment information, I would replace this map with a dedicated `Loan` entity.

This demonstrates:

```text
Start simple
    ↓
Meet current requirements
    ↓
Identify limitations
    ↓
Refactor only when requirements justify it
```

---

# 14. Possible Future Extensions

```text
1. Loan class
2. Customer borrowing history
3. Search by author
4. Search by book ID
5. Due dates
6. Reservations
7. Fine strategies
8. Cash / Card / UPI payments
9. Multiple branches
10. Database persistence
```

Possible evolved architecture:

```text
                       Library
                          │
          ┌───────────────┼────────────────┐
          │               │                │
          ▼               ▼                ▼
      BookShelf          Loan           Payment
          │               │                │
          ▼               ▼                ▼
         Book         Customer       PaymentMethod
                                            │
                                  ┌─────────┼─────────┐
                                  ▼         ▼         ▼
                                Cash      Card       UPI
```

---

# Final Takeaway

The design process followed a practical LLD workflow:

```text
Requirement
    ↓
Identify basic objects
    ↓
Implement functionality
    ↓
Use simple data structures
    ↓
Find limitations
    ↓
Introduce abstractions only when needed
```

The Version 1 implementation was functional and correctly handled the complete add → issue → return → calculate → payment flow.
