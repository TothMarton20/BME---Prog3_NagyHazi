package NagyHazi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//A CurrencyAccount osztály a deviza számlák kezelésére szolgál, amely lehetővé teszi különböző valuták egyenlegének nyilvántartását.
public class CurrencyAccount extends Account {
	private Map<String, Double> currencies; // Valuták és azok egyenlegei (pl. USD, EUR, HUF).

	// Konstruktor: Inicializálja az alapértelmezett valutákat 0 egyenleggel.
    public CurrencyAccount(String name) {
        super(name); // Az Account osztály konstruktorát hívja a tulajdonos nevével.
        currencies = new HashMap<>();
        currencies.put("USD", 0.0);
        currencies.put("EUR", 0.0);
        currencies.put("HUF", 0.0);
    }

    // Befizetés egy adott valutában.
    public void depositCurrency(String currency, double amount) {
    	// Az adott valuta egyenlegét növeli a befizetett összeggel.
        currencies.put(currency, currencies.getOrDefault(currency, 0.0) + amount);
        // Tranzakció naplózása.
        getTransactionHistory().add("Deposit: " + amount + " " + currency);
    }

    // Kifizetés egy adott valutában.
    public void withdrawCurrency(String currency, double amount) {
        double currentBalance = currencies.getOrDefault(currency, 0.0); // Aktuális egyenleg lekérése.
        if (currentBalance < amount) {
            throw new IllegalArgumentException("Insufficient funds in " + currency); // Hiba, ha nincs elég fedezet.
        }
        // Az egyenleg csökkentése.
        currencies.put(currency, currentBalance - amount);
        // Tranzakció naplózása.
        getTransactionHistory().add("Withdraw: " + amount + " " + currency);
    }
    
    // Valuta átváltása egy másik valutára.
    public void convertCurrency(String fromCurrency, String toCurrency, double amount, double exchangeRate) {
        double currentBalance = currencies.getOrDefault(fromCurrency, 0.0); // Forrás valuta egyenlege.

        if (currentBalance < amount) {
            throw new IllegalArgumentException("Insufficient funds in " + fromCurrency); // Hiba, ha nincs elég fedezet.
        }

        // Az összeget levonja a forrás valutából.
        currencies.put(fromCurrency, currentBalance - amount);

        // A cél valutához hozzáadja az átváltott összeget.
        currencies.put(toCurrency, currencies.getOrDefault(toCurrency, 0.0) + amount * exchangeRate);

        // Tranzakció naplózása.
        getTransactionHistory().add("Converted: " + amount + " " + fromCurrency + " to " + toCurrency
                + " (rate: " + exchangeRate + ")");
    }


    public Map<String, Double> getCurrencies() {
        return currencies;
    }

    // A számla adatainak mentése fájlba.
    @Override
    public void saveToFile(BufferedWriter writer) throws IOException {
        super.saveToFile(writer); // Az alap számlaadatok mentése.
        writer.write("Currency: "); // A valuták mentése.
        currencies.forEach((key, value) -> {
            try {
                writer.write(key + "=" + value + ","); // Valuta és egyenleg mentése.
            } catch (IOException e) {
                e.printStackTrace(); // Hibakezelés írás közben.
            }
        });
        writer.write("\n");
    }

    // A számla adatainak betöltése fájlból.
    @Override
    public void loadFromFile(BufferedReader reader) throws IOException {
        super.loadFromFile(reader); // Az alap számlaadatok betöltése.
        String line;
        while ((line = reader.readLine()) != null && !line.equals("===")) {
            if (line.startsWith("Currency: ")) {
            	// A valuták és egyenlegek feldolgozása.
                String[] parts = line.substring("Currency: ".length()).split(",");
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        String[] currencyData = part.split("=");
                        currencies.put(currencyData[0], Double.parseDouble(currencyData[1]));
                    }
                }
            }
        }
    }

    // A számla szöveges reprezentációja.
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.getName() + " (Currencies: "); // Név hozzáadása.
        currencies.forEach((key, value) -> sb.append(key).append("=").append(value).append(" ")); // Valuták és egyenlegek.
        sb.append(")");
        return sb.toString();
    }
}
