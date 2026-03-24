package NagyHazi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//A Bank osztály felelős a számlák kezeléséért: létrehozásért, mentésért, betöltésért.
public class Bank {
    private List<Account> accounts; // A bank által kezelt számlák listája.

 // Konstruktor: Létrehozza az üres számlalistát.
    public Bank() {
        accounts = new ArrayList<>();
    }

    public Account createAccount(String type, String name) {
        Account account;
        // Ha a típus "currency", deviza számla jön létre, egyébként normál számla.
        if ("currency".equalsIgnoreCase(type)) {
            account = new CurrencyAccount(name);
        } else {
            account = new Account(name);
        }
        accounts.add(account); // Hozzáadás a számlalistához.
        return account; // Visszatérés az új számlával.
    }

    // Visszaadja a bankhoz tartozó számlák listáját.
    public List<Account> getAccounts() {
        return accounts;
    }

    // Az összes számla adatainak mentése egy fájlba.
    public void saveAllAccounts(String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Account account : accounts) {
            	// Megadjuk a számla típusát.
                if (account instanceof CurrencyAccount) {
                    writer.write("CurrencyAccount\n");
                } else {
                    writer.write("NormalAccount\n");
                }
             // A számla adatainak írása.
                account.saveToFile(writer);
                writer.write("===\n"); // Elválasztó sor a számlák között.
            }
        }
    }

    // Az összes számla adatainak betöltése egy fájlból.
    public void loadAllAccounts(String filename) throws IOException {
        accounts.clear(); // Az aktuális számlalistát töröljük.
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            Account currentAccount = null; // Az éppen olvasott számla.

            while ((line = reader.readLine()) != null) {
            	// Új számla létrehozása típustól függően.
                if ("NormalAccount".equals(line)) {
                    currentAccount = new Account("");
                    accounts.add(currentAccount);
                } else if ("CurrencyAccount".equals(line)) {
                    currentAccount = new CurrencyAccount("");
                    accounts.add(currentAccount);
                }

                // Az aktuális számla tulajdonságainak beállítása.
                if (currentAccount != null) {
                	// Számlatulajdonos neve.
                    if (line.startsWith("Name: ")) {
                        currentAccount.setName(line.substring("Name: ".length()));
                     // Egyenleg beállítása.
                    } else if (line.startsWith("Balance: ")) {
                        currentAccount.deposit(Double.parseDouble(line.substring("Balance: ".length())));
                     // Tranzakciók hozzáadása a számlatörténethez.
                    } else if (line.startsWith("Transaction: ")) {
                        String transaction = line.substring("Transaction: ".length());
                        currentAccount.getTransactionHistory().add(transaction);
                     // Deviza számlák esetén a valuták és összegek betöltése.
                    } else if (currentAccount instanceof CurrencyAccount && line.startsWith("Currency: ")) {
                        CurrencyAccount currencyAccount = (CurrencyAccount) currentAccount;
                        String[] currencies = line.substring("Currency: ".length()).split(",");
                        for (String currency : currencies) {
                            if (!currency.isEmpty()) {
                                String[] parts = currency.split("=");
                                String currencyType = parts[0];
                                double amount = Double.parseDouble(parts[1]);
                                currencyAccount.getCurrencies().put(currencyType, amount);
                            }
                        }
                    }
                }
            }
        }
    }

}
