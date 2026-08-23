import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// ================= BOOK =================

class Book {

    private final int bookId;
    private final String name;
    private final String authorName;

    private boolean isAvailable;

    Book(int bookId, String name, String authorName) {
        this.bookId = bookId;
        this.name = name;
        this.authorName = authorName;
        this.isAvailable = true;
    }

    int getBookId() {
        return bookId;
    }

    String getName() {
        return name;
    }

    String getAuthorName() {
        return authorName;
    }

    boolean isAvailable() {
        return isAvailable;
    }

    void issueBook() {
        if (!isAvailable) {
            throw new IllegalStateException("Book is already issued");
        }

        isAvailable = false;
    }

    void returnBook() {
        isAvailable = true;
    }
}


// ================= BOOK SHELF =================

class BookShelf {

    private final int row;
    private final int column;

    private final Book[][] shelf;

    BookShelf(int row, int column) {
        this.row = row;
        this.column = column;
        this.shelf = new Book[row][column];
    }

    boolean addBook(Book book) {

        for (int i = 0; i < row; i++) {

            for (int j = 0; j < column; j++) {

                if (shelf[i][j] == null) {

                    shelf[i][j] = book;

                    return true;
                }
            }
        }

        return false;
    }

    Book findAvailableBook(String bookName) {

        for (int i = 0; i < row; i++) {

            for (int j = 0; j < column; j++) {

                Book book = shelf[i][j];

                if (book != null
                        && book.getName().equals(bookName)
                        && book.isAvailable()) {

                    return book;
                }
            }
        }

        return null;
    }

    List<Book> getBooks() {

        List<Book> books = new ArrayList<>();

        for (int i = 0; i < row; i++) {

            for (int j = 0; j < column; j++) {

                if (shelf[i][j] != null) {
                    books.add(shelf[i][j]);
                }
            }
        }

        return books;
    }
}


// ================= PERSON =================

abstract class Person {

    private final String name;
    private final String phoneNumber;

    Person(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    String getName() {
        return name;
    }

    String getPhoneNumber() {
        return phoneNumber;
    }
}


class Admin extends Person {

    Admin(String name, String phoneNumber) {
        super(name, phoneNumber);
    }
}


class Customer extends Person {

    Customer(String name, String phoneNumber) {
        super(name, phoneNumber);
    }
}


// ================= LOAN =================

class Loan {

    private final Book book;
    private final Customer customer;

    private final long issueTime;

    private long returnTime;

    private boolean isActive;

    Loan(Book book, Customer customer) {

        this.book = book;
        this.customer = customer;

        this.issueTime = System.currentTimeMillis();

        this.isActive = true;
    }

    Book getBook() {
        return book;
    }

    Customer getCustomer() {
        return customer;
    }

    long getIssueTime() {
        return issueTime;
    }

    boolean isActive() {
        return isActive;
    }

    long closeLoan() {

        if (!isActive) {
            throw new IllegalStateException("Loan already closed");
        }

        returnTime = System.currentTimeMillis();

        isActive = false;

        return returnTime - issueTime;
    }
}


// ================= PAYMENT =================

class Payment {

    private final int amount;

    private boolean isPaid;

    Payment(int amount) {
        this.amount = amount;
        this.isPaid = false;
    }

    boolean processPayment() {

        System.out.println("Processing payment: ₹" + amount);

        // Assume payment succeeds
        isPaid = true;

        return true;
    }

    boolean isPaid() {
        return isPaid;
    }

    int getAmount() {
        return amount;
    }
}


// ================= LIBRARY =================

class Library {

    private final List<BookShelf> shelves;

    // Active loans
    private final Map<Integer, Loan> activeLoans;

    // Optional: preserve history
    private final List<Loan> loanHistory;

    Library() {

        shelves = new ArrayList<>();

        activeLoans = new HashMap<>();

        loanHistory = new ArrayList<>();
    }


    // ---------- ADMIN OPERATION ----------

    void addShelf(BookShelf shelf) {

        shelves.add(shelf);
    }


    void addBook(Book book, Admin admin) {

        for (BookShelf shelf : shelves) {

            if (shelf.addBook(book)) {

                System.out.println(
                        book.getName() + " added to library"
                );

                return;
            }
        }

        System.out.println("No space available for book");
    }


    // ---------- ISSUE BOOK ----------

    boolean issueBook(
            String bookName,
            Customer customer
    ) {

        for (BookShelf shelf : shelves) {

            Book book = shelf.findAvailableBook(bookName);

            if (book != null) {

                book.issueBook();

                Loan loan =
                        new Loan(book, customer);

                activeLoans.put(
                        book.getBookId(),
                        loan
                );

                loanHistory.add(loan);

                System.out.println(
                        "Book issued: " +
                        book.getName()
                );

                System.out.println(
                        "Issued to: " +
                        customer.getName()
                );

                return true;
            }
        }

        System.out.println(
                "Book not available"
        );

        return false;
    }


    // ---------- RETURN BOOK ----------

    int returnBook(int bookId) {

        Loan loan =
                activeLoans.get(bookId);

        if (loan == null) {

            System.out.println(
                    "No active loan found"
            );

            return -1;
        }


        // Calculate borrowed duration

        long totalTime =
                loan.closeLoan();


        long millisecondsPerDay =
                1000L * 60 * 60 * 24;


        long days =
                totalTime / millisecondsPerDay;


        // Minimum one day charge

        days = Math.max(1, days);


        int amount =
                (int) days * 10;


        // Create payment

        Payment payment =
                new Payment(amount);


        if (!payment.processPayment()) {

            System.out.println(
                    "Payment failed"
            );

            return -1;
        }


        // Make book available

        Book book = loan.getBook();

        book.returnBook();


        // Remove from active loans

        activeLoans.remove(bookId);


        System.out.println(
                "Book returned successfully"
        );

        System.out.println(
                "Amount paid: ₹" +
                payment.getAmount()
        );

        return amount;
    }


    void showActiveLoans() {

        for (Loan loan
                : activeLoans.values()) {

            System.out.println(

                    loan.getBook().getName()
                    + " -> "
                    + loan.getCustomer().getName()

            );
        }
    }
}


// ================= MAIN =================

public class LibraryManagementSystem {

    public static void main(String[] args) {

        // Create admin

        Admin admin =
                new Admin(
                        "John",
                        "9999999999"
                );


        // Create customer

        Customer customer =
                new Customer(
                        "Alice",
                        "8888888888"
                );


        // Create library

        Library library =
                new Library();


        // Create shelves

        BookShelf shelf1 =
                new BookShelf(2, 3);

        library.addShelf(shelf1);


        // Create books

        Book book1 =
                new Book(
                        1,
                        "Clean Code",
                        "Robert Martin"
                );


        Book book2 =
                new Book(
                        2,
                        "Design Patterns",
                        "Gang of Four"
                );


        Book book3 =
                new Book(
                        3,
                        "Clean Code",
                        "Robert Martin"
                );


        // Admin adds books

        library.addBook(book1, admin);

        library.addBook(book2, admin);

        library.addBook(book3, admin);


        // Customer borrows Clean Code

        library.issueBook(
                "Clean Code",
                customer
        );


        // Show active loans

        System.out.println("\nActive Loans:");

        library.showActiveLoans();


        // Return book

        System.out.println("\nReturning book...");

        library.returnBook(1);
    }
}