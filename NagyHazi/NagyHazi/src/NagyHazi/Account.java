package NagyHazi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//Az Account osztály egy alapvető bankszámla modellezésére szolgál, amely tartalmaz egy tulajdonos nevet, egyenleget és tranzakciós történetet.
public class Account {
    private String name; // A számla tulajdonosának neve.
    private double balance; // A számla aktuális egyenlege.
    private List<String> transactionHistory; // A számlához tartozó tranzakciók története.

    // Konstruktor: Létrehozza az új számlát a tulajdonos nevével, 0 egyenleggel és üres tranzakciós listával.
    public Account(String name) {
        this.name = name;
        this.balance = 0.0;
        this.transactionHistory = new ArrayList<>();
    }

    // Befizetés a számlára.
    public void deposit(double amount) {
        balance += amount;
        transactionHistory.add("Deposit: " + amount + " HUF");
    }

    // Kifizetés a számláról.
    public void withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            transactionHistory.add("Withdraw: " + amount + " HUF");
        } else {
            transactionHistory.add("Failed Withdrawal: " + amount + " HUF (Insufficient funds)");
        }
    }

    // Visszaadja a számla tulajdonosának nevét.
    public String getName() {
        return name;
    }

    // Visszaadja a számla aktuális egyenlegét.
    public double getBalance() {
        return balance;
    }

    // Visszaadja a számlához tartozó tranzakciók listáját.
    public List<String> getTransactionHistory() {
        return transactionHistory;
    }

    // A számla adatainak mentése egy fájlba.
    public void saveToFile(BufferedWriter writer) throws IOException {
        writer.write("Name: " + name + "\n");
        writer.write("Balance: " + balance + "\n");
        for (String transaction : transactionHistory) {
            writer.write("Transaction: " + transaction + "\n");
        }
    }

    // A számla adatainak betöltése egy fájlból.
    public void loadFromFile(BufferedReader reader) throws IOException {
        String line;
        if ((line = reader.readLine()) != null && line.startsWith("Name: ")) {
            name = line.split(": ")[1];
        }
        if ((line = reader.readLine()) != null && line.startsWith("Balance: ")) {
            balance = Double.parseDouble(line.split(": ")[1]);
        }

        transactionHistory.clear();
        while ((line = reader.readLine()) != null && !line.equals("===")) {
            if (line.startsWith("Transaction: ")) {
                transactionHistory.add(line.substring("Transaction: ".length()));
            }
        }
    }
    
    // A számla tulajdonosának nevének módosítása.
    public void setName(String name) {
        this.name = name;
    }

    // A számla szöveges reprezentációja: név és egyenleg.
    @Override
    public String toString() {
        return name + " (Balance: " + balance + " HUF)";
    }
}
