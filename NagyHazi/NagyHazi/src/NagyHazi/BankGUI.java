package NagyHazi;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

//A BankGUI osztály egy Swing-alapú grafikus felület, amely lehetővé teszi a banki funkciók kezelését.
@SuppressWarnings({ "serial" })
public class BankGUI extends JFrame {
    private static final String DATA_FILE = "accounts.txt"; // Az adatok mentéséhez és betöltéséhez használt fájl neve.
    private Bank bank; // A Bank osztály példánya a számlák kezelésére.

    // GUI komponensek
    private JList<Account> normalAccountList; // Normál számlák listája.
    private JList<CurrencyAccount> currencyAccountList; // Deviza számlák listája.

    private DefaultListModel<Account> normalAccountListModel; // Normál számlák listájának modellje.
    private DefaultListModel<CurrencyAccount> currencyAccountListModel; // Deviza számlák listájának modellje.

    private JComboBox<String> depositCurrencySelector; // Befizetésnél választható valuta.
    private JComboBox<Account> transferToSelector; // Átutalás cél számlájának kiválasztása.
    private JComboBox<String> fromCurrencySelector; // Átváltás forrás valutája.
    private JComboBox<String> toCurrencySelector; // Átváltás cél valutája.

    private JTextArea transactionLog; // Tranzakciós napló megjelenítésére.
    private JTextField amountField, nameField; // Összeg és név bevitelére szolgáló mezők.
    private JTextField convertAmountField; // Átváltandó összeg bevitelére szolgáló mező.

    private JLabel exchangeRatesLabel; // Árfolyamok megjelenítése.

    // Konstruktor: Inicializálja a bankot, beállítja a GUI-t, betölti a számlákat és megjeleníti az ablakot.
    public BankGUI() {
        normalAccountListModel = new DefaultListModel<>();
        currencyAccountListModel = new DefaultListModel<>();

        normalAccountList = new JList<>(normalAccountListModel);
        currencyAccountList = new JList<>(currencyAccountListModel);

        depositCurrencySelector = new JComboBox<>(new String[]{"HUF", "USD", "EUR"});
        transferToSelector = new JComboBox<>();
        fromCurrencySelector = new JComboBox<>(new String[]{"HUF", "USD", "EUR"});
        toCurrencySelector = new JComboBox<>(new String[]{"HUF", "USD", "EUR"});

        bank = new Bank();

        setupGUI(); // GUI komponensek elrendezése.
        loadAccounts(); // Számlák betöltése fájlból.
        refreshAccountLists(); // GUI frissítése az aktuális számlalistával.

        setTitle("Bank System");
        setSize(1600, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true); // Az ablak láthatóvá tétele.
    }

    // A GUI komponenseinek létrehozása és elrendezése.
    private void setupGUI() {
    	setupMenu(); // Menü létrehozása.
        setLayout(new BorderLayout());

        // Bal panel: Két külön lista (Normál és devizaszámlák)
        JPanel leftPanel = new JPanel(new GridLayout(2, 1));
        JScrollPane normalAccountScrollPane = new JScrollPane(normalAccountList);
        JScrollPane currencyAccountScrollPane = new JScrollPane(currencyAccountList);

        normalAccountScrollPane.setBorder(BorderFactory.createTitledBorder("Normal Accounts"));
        currencyAccountScrollPane.setBorder(BorderFactory.createTitledBorder("Currency Accounts"));

        leftPanel.add(normalAccountScrollPane);
        leftPanel.add(currencyAccountScrollPane);

        // ListSelectionListener hozzáadása
        normalAccountList.addListSelectionListener(e -> handleListSelection(e, currencyAccountList));
        currencyAccountList.addListSelectionListener(e -> handleListSelection(e, normalAccountList));

        add(leftPanel, BorderLayout.WEST);

        // Felső panel: Számla létrehozása
        JPanel topPanel = new JPanel(new FlowLayout());
        nameField = new JTextField(15);
        JButton createNormalAccountButton = new JButton("Create Normal Account");
        JButton createCurrencyAccountButton = new JButton("Create Currency Account");
        topPanel.add(new JLabel("Name:"));
        topPanel.add(nameField);
        topPanel.add(createNormalAccountButton);
        topPanel.add(createCurrencyAccountButton);

        add(topPanel, BorderLayout.NORTH);

        // Középső panel: Tranzakciós napló
        transactionLog = new JTextArea();
        transactionLog.setEditable(false);
        JScrollPane transactionLogScrollPane = new JScrollPane(transactionLog);
        transactionLogScrollPane.setBorder(BorderFactory.createTitledBorder("Transaction Log"));
        add(transactionLogScrollPane, BorderLayout.CENTER);

        // Alsó panel: Tranzakciók kezelése
        JPanel bottomPanel = new JPanel(new FlowLayout());
        amountField = new JTextField(10);
        convertAmountField = new JTextField(10);
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton transferButton = new JButton("Transfer");
        
        fromCurrencySelector = new JComboBox<>(new String[]{"HUF", "USD", "EUR"});
        toCurrencySelector = new JComboBox<>(new String[]{"HUF", "USD", "EUR"});

        // Állítsuk be az alapértelmezett értékeket
        fromCurrencySelector.setSelectedIndex(0); // Alapértelmezett: HUF
        toCurrencySelector.setSelectedIndex(1); // Alapértelmezett: USD

        JButton convertCurrencyButton = new JButton("Convert Currency");
        
        JButton exitButton = new JButton("Exit");

        bottomPanel.add(new JLabel("Amount:"));
        bottomPanel.add(amountField);
        bottomPanel.add(new JLabel("Currency:"));
        bottomPanel.add(depositCurrencySelector);
        bottomPanel.add(depositButton);
        bottomPanel.add(withdrawButton);
        bottomPanel.add(new JLabel("Transfer to:"));
        bottomPanel.add(transferToSelector);
        bottomPanel.add(transferButton);
        bottomPanel.add(new JLabel("Convert Amount:"));
        bottomPanel.add(convertAmountField);
        bottomPanel.add(new JLabel("From:"));
        bottomPanel.add(fromCurrencySelector);
        bottomPanel.add(new JLabel("To:"));
        bottomPanel.add(toCurrencySelector);
        bottomPanel.add(convertCurrencyButton);

        bottomPanel.add(exitButton); // Kilépés gomb hozzáadása

        add(bottomPanel, BorderLayout.SOUTH);

        // Jobb panel: Valutaárfolyamok
        JPanel rightPanel = new JPanel(new BorderLayout());
        exchangeRatesLabel = new JLabel(getExchangeRatesText(), SwingConstants.CENTER);
        rightPanel.add(exchangeRatesLabel, BorderLayout.NORTH);
        add(rightPanel, BorderLayout.EAST);

        // Eseménykezelők hozzáadása
        createNormalAccountButton.addActionListener(e -> createAccount("normal"));
        createCurrencyAccountButton.addActionListener(e -> createAccount("currency"));
        depositButton.addActionListener(e -> deposit());
        withdrawButton.addActionListener(e -> withdraw());
        transferButton.addActionListener(e -> transfer());
        convertCurrencyButton.addActionListener(e -> convertCurrency());
        exitButton.addActionListener(e -> exitApplication()); // Kilépés gomb funkció
    }
    
    private void deposit() {
        Account account = getSelectedAccount(); // Kiválasztott számla lekérése.
        if (account == null) { // Ha nincs számla kiválasztva, hibaüzenet.
            showError("No account selected.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText()); // Bevitt összeg lekérése.
            String currency = (String) depositCurrencySelector.getSelectedItem(); // Választott valuta lekérése.

            if (account instanceof CurrencyAccount) {
            	// Ha devizaszámláról van szó, deviza alapján végez befizetést.
                ((CurrencyAccount) account).depositCurrency(currency, amount);
                transactionLog.append("Deposited " + amount + " " + currency + " to " + account.getName() + ".\n");
            } else if ("HUF".equals(currency)) {
            	// Normál számla esetén csak HUF valutában lehet befizetni.
                account.deposit(amount);
                transactionLog.append("Deposited " + amount + " HUF to " + account.getName() + ".\n");
            } else {
            	// Hibakezelés, ha nem HUF valutát próbálnak befizetni normál számlára.
                showError("Cannot deposit non-HUF currency to a normal account.");
            }
            refreshAccountLists(); // GUI frissítése.
        } catch (NumberFormatException e) {
        	// Hibakezelés érvénytelen összeg bevitele esetén.
            showError("Invalid amount. Please enter a valid number.");
        }
    }
    
    private void withdraw() {
    	Account account = getSelectedAccount(); // Kiválasztott számla lekérése.
        if (account == null) { // Ha nincs számla kiválasztva, hibaüzenet.
            showError("No account selected.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText()); // Bevitt összeg lekérése.
            String currency = (String) depositCurrencySelector.getSelectedItem(); // Választott valuta lekérése.

            if (account instanceof CurrencyAccount) {
                // Ha devizaszámláról van szó, deviza alapján végez kifizetést.
                CurrencyAccount currencyAccount = (CurrencyAccount) account;
                currencyAccount.withdrawCurrency(currency, amount);
                transactionLog.append("Withdrew " + amount + " " + currency + " from " + account.getName() + ".\n");
            } else if ("HUF".equals(currency)) {
                // Normál számla esetén csak HUF valutában lehet kifizetni.
                account.withdraw(amount);
                transactionLog.append("Withdrew " + amount + " HUF from " + account.getName() + ".\n");
            } else {
                // Hibakezelés, ha nem HUF valutát próbálnak kifizetni normál számláról.
                showError("Cannot withdraw non-HUF currency from a normal account.");
            }
            refreshAccountLists(); // GUI frissítése.
        } catch (NumberFormatException e) {
            // Hibakezelés érvénytelen összeg bevitele esetén.
            showError("Invalid amount. Please enter a valid number.");
        }
    }
    
    private void transfer() {
    	Account fromAccount = getSelectedAccount(); // Forrásszámla lekérése.
        if (fromAccount == null) { // Ha nincs számla kiválasztva, hibaüzenet.
            showError("No source account selected.");
            return;
        }

        Account toAccount = (Account) transferToSelector.getSelectedItem(); // Cél számla lekérése.
        if (toAccount == null) { // Ha nincs cél számla kiválasztva, hibaüzenet.
            showError("No destination account selected.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText()); // Bevitt összeg lekérése.
            String currency = (String) depositCurrencySelector.getSelectedItem(); // Választott valuta lekérése.

            if (fromAccount instanceof CurrencyAccount && toAccount instanceof CurrencyAccount) {
            	// Devizaszámlák közötti átutalás kezelése.
                CurrencyAccount fromCurrencyAccount = (CurrencyAccount) fromAccount;
                CurrencyAccount toCurrencyAccount = (CurrencyAccount) toAccount; 

                fromCurrencyAccount.withdrawCurrency(currency, amount); // Kivonás a forrásszámláról.
                toCurrencyAccount.depositCurrency(currency, amount); // Befizetés a célszámlára.

                transactionLog.append("Transferred " + amount + " " + currency + " from " + fromAccount.getName()
                        + " to " + toAccount.getName() + ".\n");
            } else if (!(fromAccount instanceof CurrencyAccount) && "HUF".equals(currency)) {
            	// Normál számlák közötti átutalás HUF valutában.
                fromAccount.withdraw(amount);
                toAccount.deposit(amount);

                transactionLog.append("Transferred " + amount + " HUF from " + fromAccount.getName()
                        + " to " + toAccount.getName() + ".\n");
            } else {
            	// Hibakezelés, ha az átutalás nem megfelelő számlák között történik.
                showError("Currency mismatch or invalid transfer between account types.");
            }
            refreshAccountLists(); // GUI frissítése.
        } catch (NumberFormatException e) {
        	// Hibakezelés érvénytelen összeg bevitele esetén.
            showError("Invalid amount. Please enter a valid number.");
        }
    }

    private void handleListSelection(ListSelectionEvent e, JList<?> otherList) {
        if (!e.getValueIsAdjusting()) {
            otherList.clearSelection(); // Másik lista kijelölésének törlése
            updateTransactionLog(getSelectedAccount());
        }
    }

    private String getExchangeRatesText() {
        return "<html><h3>Exchange Rates:</h3><br>USD -> HUF = 380<br>EUR -> HUF = 400</html>";
    }

    private void createAccount(String type) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Name cannot be empty.");
            return;
        }
        bank.createAccount(type, name);
        refreshAccountLists();
        transactionLog.append(type + " account for " + name + " created.\n");
        nameField.setText("");
    }

    private Account getSelectedAccount() {
        Account selected = normalAccountList.getSelectedValue();
        if (selected == null) {
            selected = currencyAccountList.getSelectedValue();
        }
        return selected;
    }

    private void saveAccounts() {
        try {
            bank.saveAllAccounts(DATA_FILE);
        } catch (IOException e) {
            showError("Error saving accounts: " + e.getMessage());
        }
    }

    private void loadAccounts() {
        try {
            bank.loadAllAccounts(DATA_FILE);
        } catch (IOException e) {
            showError("Error loading accounts: " + e.getMessage());
        }
        refreshAccountLists();
    }
    
    private void convertCurrency() {
        Account selectedAccount = getSelectedAccount(); // Kiválasztott számla lekérése.

        if (selectedAccount instanceof CurrencyAccount) {
            try {
            	String fromCurrency = (String) fromCurrencySelector.getSelectedItem(); // Forrás valuta.
                String toCurrency = (String) toCurrencySelector.getSelectedItem(); // Cél valuta.
                double amount = Double.parseDouble(convertAmountField.getText()); // Átváltandó összeg.

                // Hibakezelés: valuta kiválasztása kötelező.
                if (fromCurrency == null || toCurrency == null) {
                    showError("Please select both currencies for conversion.");
                    return;
                }

                // Hibakezelés: forrás és cél valuta nem lehet ugyanaz.
                if (fromCurrency.equals(toCurrency)) {
                    showError("Cannot convert the same currency.");
                    return;
                }

                // Árfolyam lekérése és átváltás végrehajtása.
                double exchangeRate = getExchangeRate(fromCurrency, toCurrency);
                CurrencyAccount currencyAccount = (CurrencyAccount) selectedAccount;
                currencyAccount.convertCurrency(fromCurrency, toCurrency, amount, exchangeRate);

                transactionLog.append("Converted " + amount + " " + fromCurrency + " to " + toCurrency + " at rate " + exchangeRate + ".\n");
                refreshAccountLists(); // GUI frissítése.
            } catch (NumberFormatException ex) {
            	// Hibakezelés érvénytelen összeg bevitele esetén.
                showError("Invalid amount. Please enter a valid number.");
            } catch (IllegalArgumentException ex) {
            	// Hibakezelés nem támogatott árfolyam esetén.
                showError(ex.getMessage());
            }
        } else {
        	// Hibakezelés: átváltás csak devizaszámlákon lehetséges.
            showError("Only currency accounts can convert currencies.");
        }
    }
    
    private double getExchangeRate(String fromCurrency, String toCurrency) {
    	// Árfolyamok kezelése különböző valuták között.
        if ("HUF".equals(fromCurrency) && "USD".equals(toCurrency)) return 0.0027;
        if ("HUF".equals(fromCurrency) && "EUR".equals(toCurrency)) return 0.0025;
        if ("USD".equals(fromCurrency) && "HUF".equals(toCurrency)) return 370.0;
        if ("EUR".equals(fromCurrency) && "HUF".equals(toCurrency)) return 400.0;
        if ("USD".equals(fromCurrency) && "EUR".equals(toCurrency)) return 0.93;
        if ("EUR".equals(fromCurrency) && "USD".equals(toCurrency)) return 1.08;

        // Hibakezelés, ha az árfolyam nem érhető el.
        throw new IllegalArgumentException("Exchange rate not available for these currencies.");
    }

    private void refreshAccountLists() {
        normalAccountListModel.clear();
        currencyAccountListModel.clear();
        transferToSelector.removeAllItems();

        for (Account account : bank.getAccounts()) {
            if (account instanceof CurrencyAccount) {
                currencyAccountListModel.addElement((CurrencyAccount) account);
            } else {
                normalAccountListModel.addElement(account);
            }
            transferToSelector.addItem(account);
        }

        transactionLog.setText("Accounts refreshed.");
    }

    private void updateTransactionLog(Account account) {
        transactionLog.setText(""); // Töröljük a korábbi tranzakciókat
        if (account == null) return;

        transactionLog.append("Transactions for " + account.getName() + ":\n");
        for (String transaction : account.getTransactionHistory()) {
            transactionLog.append(transaction + "\n");
        }

        if (account instanceof CurrencyAccount) {
            CurrencyAccount currencyAccount = (CurrencyAccount) account;
            transactionLog.append("\nCurrencies:\n");
            for (Map.Entry<String, Double> entry : currencyAccount.getCurrencies().entrySet()) {
                transactionLog.append(entry.getKey() + ": " + entry.getValue() + "\n");
            }
        }
    }
    
    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Help Menü
        JMenu helpMenu = new JMenu("Help");
        JMenuItem functionsMenuItem = new JMenuItem("Funkciók leírása");

        functionsMenuItem.addActionListener(e -> showHelpDialog());
        helpMenu.add(functionsMenuItem);

        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void showHelpDialog() {
        String message = """
            Alapvető funkciók:
            1. Számla létrehozása:
               - Írj be egy nevet, majd kattints a "Create Normal Account" vagy
                 "Create Currency Account" gombra.
            
            2. Befizetés:
               - Válaszd ki a számlát a bal oldali listából.
               - Add meg az összeget és a valutát, majd kattints a "Deposit" gombra.

            3. Kivét:
               - Válaszd ki a számlát, add meg az összeget és kattints a "Withdraw" gombra.

            4. Átváltás:
               - Csak devizaszámlák esetén.
               - Add meg az összeget, válaszd ki a forrás és cél valutát,
                 majd kattints a "Convert Currency" gombra.

            5. Átutalás:
               - Válaszd ki az egyik számlát (forrás), majd a "Transfer to" mezőben a célszámlát.
               - Add meg az összeget, majd kattints a "Transfer" gombra.

            6. Adatok mentése és betöltése:
               - A számlák automatikusan mentésre kerülnek bezáráskor.
               - Az adatok betöltése a program indításakor automatikusan megtörténik.
            """;
        JOptionPane.showMessageDialog(this, message, "Funkciók leírása", JOptionPane.INFORMATION_MESSAGE);
    }


    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void exitApplication() {
        saveAccounts();
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankGUI::new);
    }
}
