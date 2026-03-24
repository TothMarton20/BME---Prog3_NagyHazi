package NagyHazi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
public class NagyHaziTest {

    private Account account;
    private CurrencyAccount currencyAccount;
    private Bank bank;

    @BeforeEach
    public void setUp() {
        account = new Account("TestAccount");
        currencyAccount = new CurrencyAccount("TestCurrencyAccount");
        bank = new Bank();
    }

    @Test
    public void testAccountDeposit() {
        account.deposit(1000);
        assertEquals(1000, account.getBalance());
    }

    @Test
    public void testAccountWithdrawSuccess() {
        account.deposit(1000);
        account.withdraw(500);
        assertEquals(500, account.getBalance());
    }

    @Test
    public void testAccountWithdrawFailure() {
        account.deposit(1000);
        account.withdraw(1500);
        assertEquals(1000, account.getBalance());
        assertTrue(account.getTransactionHistory().contains("Failed Withdrawal: 1500.0 HUF (Insufficient funds)"));
    }

    @Test
    public void testCurrencyAccountDepositCurrency() {
        currencyAccount.depositCurrency("USD", 500);
        assertEquals(500, currencyAccount.getCurrencies().get("USD"));
    }

    @Test
    public void testCurrencyAccountWithdrawCurrencySuccess() {
        currencyAccount.depositCurrency("USD", 500);
        currencyAccount.withdrawCurrency("USD", 300);
        assertEquals(200, currencyAccount.getCurrencies().get("USD"));
    }

    @Test
    public void testCurrencyAccountWithdrawCurrencyFailure() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            currencyAccount.withdrawCurrency("USD", 300);
        });
        assertEquals("Insufficient funds in USD", exception.getMessage());
    }

    @Test
    public void testCurrencyAccountConvertCurrency() {
        currencyAccount.depositCurrency("USD", 500);
        currencyAccount.convertCurrency("USD", "HUF", 100, 370.0);
        assertEquals(400, currencyAccount.getCurrencies().get("USD"));
        assertEquals(37000, currencyAccount.getCurrencies().get("HUF"));
    }

    @Test
    public void testBankCreateAccount() {
        Account normalAccount = bank.createAccount("normal", "NormalAccount");
        Account currencyAccount = bank.createAccount("currency", "CurrencyAccount");
        assertEquals(2, bank.getAccounts().size());
        assertTrue(normalAccount instanceof Account);
        assertTrue(currencyAccount instanceof CurrencyAccount);
    }

    @Test
    public void testBankSaveAndLoadAccounts() throws Exception {
        // Létrehozunk számlákat és hozzáadunk tranzakciókat
        Account normalAccount = bank.createAccount("normal", "NormalAccount");
        normalAccount.deposit(1000);
        CurrencyAccount currencyAccount = (CurrencyAccount) bank.createAccount("currency", "CurrencyAccount");
        currencyAccount.depositCurrency("USD", 500);

        // Mentés fájlba
        String filename = "test_accounts.txt";
        bank.saveAllAccounts(filename);

        // Új Bank példány és betöltés
        Bank loadedBank = new Bank();
        loadedBank.loadAllAccounts(filename);

        // Ellenőrzések
        List<Account> accounts = loadedBank.getAccounts();
        assertEquals(2, accounts.size());
        assertEquals("NormalAccount", accounts.get(0).getName());
        assertEquals(1000, accounts.get(0).getBalance());
        assertEquals(500, ((CurrencyAccount) accounts.get(1)).getCurrencies().get("USD"));
    }

    @Test
    public void testTransactionHistory() {
        // Normál számla tranzakciók
        account.deposit(1000);
        account.withdraw(500);
        account.withdraw(700); // Sikertelen kivonás

        List<String> accountHistory = account.getTransactionHistory();
        assertEquals(3, accountHistory.size());
        assertEquals("Deposit: 1000.0 HUF", accountHistory.get(0));
        assertEquals("Withdraw: 500.0 HUF", accountHistory.get(1));
        assertEquals("Failed Withdrawal: 700.0 HUF (Insufficient funds)", accountHistory.get(2));

        // Devizaszámla tranzakciók
        currencyAccount.depositCurrency("USD", 100);
        currencyAccount.withdrawCurrency("USD", 50);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            currencyAccount.withdrawCurrency("USD", 100);
        });
        assertEquals("Insufficient funds in USD", exception.getMessage());

        List<String> currencyHistory = currencyAccount.getTransactionHistory();
        assertEquals(2, currencyHistory.size());
        assertEquals("Deposit: 100.0 USD", currencyHistory.get(0));
        assertEquals("Withdraw: 50.0 USD", currencyHistory.get(1));
    }


}
