#Bank System (Java)

A simple banking system implemented in Java with support for normal and currency accounts, transaction handling, and a graphical user interface.

---

##Features

- Create and manage **normal (HUF) accounts**
- Support for **multi-currency accounts** (HUF, USD, EUR)
- Perform transactions:
  - Deposit
  - Withdraw
  - Transfer between accounts
  - Currency conversion
- View **transaction history**
- Save and load account data from file
- User-friendly **Swing GUI**

---

##Technologies

- **Java**
- **Swing** (GUI)
- **JUnit 5** (testing)

---

##Project Structure

- `Account` – basic account functionality (deposit, withdraw, history)
- `CurrencyAccount` – extended account with multi-currency support
- `Bank` – manages accounts and data persistence
- `BankGUI` – graphical user interface
- `NagyHaziTest` – unit tests for the system
