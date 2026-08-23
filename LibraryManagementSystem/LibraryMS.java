import java.beans.Customizer;
import java.util.ArrayList;
import java.util.HashMap;

class Book {
    private int bookId;
    private String name;
    private String authorName;
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
        isAvailable = false;
    }

    void returnBook() {
        isAvailable = true;
    }
}



class BookShelf{
    int row;
    int column;
    Book [][] shelf;

    BookShelf(int row, int column){
        this.row = row;
        this.column = column;
        shelf = new Book[row][column];
    }

    boolean addBook(Book b , Person p){
        if(p.whoAmI() == "Admin"){
            for(int i = 0 ; i < row ; i++){
                for(int j = 0 ; j < column ;j++){
                    if(shelf[i][j] == null){
                        shelf[i][j] = b;
                        return true;
                    }
                }
            }
            System.out.println("totally filled with books");
            return false;
        }
        else{
            System.out.println("You are not authorized for this action");
            return false;
        }
    }
    boolean IssueBook(Book b, Person p){
        if(p.whoAmI() == "Admin"){
            if(!b.isAvailable()){
                return false;
            }
            for(int i = 0 ; i < row ; i++){
                for(int j = 0 ; j < column ;j++){
                    if(shelf[i][j] == b){
                        b.issueBook();
                        return true;
                    }
                }
            }
            System.out.println("cannot issue");
            return false;
        }
        else{
            System.out.println("You are not authorized for this action");
            return false;
        }
    }
}

class Library{
    ArrayList<BookShelf> shelves = new ArrayList<>();
    Admin a;

    HashMap<Book , Long> hm = new HashMap<>();

    String [] rule = {"You are not allowed to talk",
            "You are not allowed to eat inside the library"};

    Library(Admin p){
        a = p;
    }

    void addShelf(BookShelf b){
        shelves.add(b);
    }

    boolean issue(String bookName) {
        for (BookShelf bookShelf : shelves) {

            for (int i = 0; i < bookShelf.row; i++) {
                for (int j = 0; j < bookShelf.column; j++) {

                    Book book = bookShelf.shelf[i][j];

                    if (book != null
                            && book.getName().equals(bookName)
                            && book.isAvailable()) {

                        System.out.println("Book issued");

                        book.issueBook();
                        hm.put(book , System.currentTimeMillis());

                        return true;
                    }
                }
            }
        }

        System.out.println("Book not present or not available");
        return false;
    }

    int submit(String bookName) {

        for (BookShelf bookShelf : shelves) {

            for (int i = 0; i < bookShelf.row; i++) {
                for (int j = 0; j < bookShelf.column; j++) {

                    Book book = bookShelf.shelf[i][j];

                    if (book != null
                            && book.getName().equals(bookName)
                            && !book.isAvailable()) {

                        long issueTime = hm.get(book);

                        long returnTime = System.currentTimeMillis();

                        long totalTime =
                                returnTime - issueTime;

                        // Convert milliseconds to days
                        long days = totalTime /
                                (1000 * 60 * 60 * 24);

                        // Example: ₹10 per day
                        int amount = (int) days * 10;

                        // Make book available again
                        book.returnBook();

                        // Remove active issue record
                        hm.remove(book);

                        System.out.println("Book returned");
                        System.out.println("Total amount: " + amount);

                        return amount;
                    }
                }
            }
        }

        System.out.println("Book not found or not issued");
        return -1;
    }
}


abstract class Person{
    String name;
    String phn_no;
    abstract String whoAmI();
}

class Admin extends Person{
    Admin(String name , String phn_no){
        this.name = name;
        this.phn_no = phn_no;
    }
    String whoAmI(){
        return "Admin";
    }
}

class Customer extends Person{
    Customer(String name , String phn_no){
        this.name = name;
        this.phn_no = phn_no;
    }
    String whoAmI(){
        return "Customer";
    }
}

class Payment {

    private int amount;
    private boolean isPaid;

    Payment(int amount) {
        this.amount = amount;
        this.isPaid = false;
    }

    boolean processPayment() {

        if (amount <= 0) {
            isPaid = true;
            return true;
        }

        // For now, assume payment succeeds
        System.out.println("Processing payment: " + amount);

        isPaid = true;

        return true;
    }

    boolean isPaid() {
        return isPaid;
    }
}


public class LibraryManagementSystem {
    public static void main(String[] args) {

    }
}
