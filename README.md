# uwallet

## Docs
[The javadocs for the uwallet package](http://htmlpreview.github.io/?https://github.com/zdalih/uwallet/blob/master/javadoc/uwallet/Wallet.html)

## Usage

The main user interface is the ```uwallet.Wallet``` class. a [Wallet](https://htmlpreview.github.io/?https://raw.githubusercontent.com/zdalih/uwallet/master/javadoc/uwallet/Wallet.html) object can be constructed given what must be a unique id and a region code. A wallet is a collection of 'accounts' which are entities that hold balances and can be deposited to / withdrawn from.

The unique id is used to tell one wallet apart from another - and to be able to load wallets from record. When creating wallets ensure that you use a unique id that is suitable for your case. For example a user id if the user is to only have a single wallet.

The region code will be used to format balances within the wallet to the respective currency norms in such region. This region code is a [ISO 3166 alpha-2 country code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2) or [UN M.49 numeric-3 area code](https://en.wikipedia.org/wiki/UN_M.49).

```java
Wallet wallet = new Wallet("ID001", "US");
```
Creation of a wallet will create a record in the persistent data. This allows one to get a ```Wallet``` object by it's id.

```java
Wallet wallet = Wallet.loadWallet("ID001")
```

#### Wallet: Creating Accounts
Now we may start adding new accounts to this wallet. Accounts are identified again by a name -  a wallet can not have two accounts with the same name. 

```java
wallet.createNewAccount("chequing");
wallet.createNewAccount("savings");
```

We can now call on to one of the following mechanisms onto accounts:
* ``` wallet.depositTo(1000.00, "chequing"); ```
* ``` wallet.withdrawFrom(500.0, "chequing"); ``` - will not allow the account's balance to drop below 0.
* ``` wallet.transfer(500.0, "chequing", "savings"); ```  - will not allow the 'from' account's balance to drop below 0.

For each of these methods, one may add an optional description by adding a description string at the end of the method.


```java
wallet.depositTo(600.0, "chequing", "a description");
```

#### Wallet: Getting the Balance

One can get the balance as formatted by regional customs, or simple as a BigDecimal:

```java
wallet.depositTo(600.0, "chequing", "a description");
String balance = wallet.getAccountBalanceFormatted("chequing"); // returns "$600.00"
BigDecimal balance = wallet.getAccountBalanceBigDecimal("chequing"); //returns new BigDecimal("600.0")
```

#### Wallet: Getting Last N Transactions

One can fetch the last N transactions for a given account by call to the followinig method which in this case will return a ```List<Transaction>``` of the past 10 transactions in the account 'chequing'.

```java
List<Transaction> pastTx = wallet.getLastNTransactions("chequing", 10);
```

The [Transaction](https://htmlpreview.github.io/?https://raw.githubusercontent.com/zdalih/uwallet/master/javadoc/uwallet/Transaction.html) is an immutable object that contains a globally unique identifier, a timestamp refering to the time the transaction took place (~1ms uncertainty), the amount of the transaction, the nature of the transaction, and a description of the transaction if one exists.

#### Wallet: Exceptions

Exceptions will be thrown when one tries to:
* Do an operation on an account or wallet that does not exist (```NoSuchObjectInDatabaseException```)
* Create an account with an already existing name for a wallet (```UniqueAccountIDConstraintException```)
* Withdraw money from an account with insufficient funds (```UniqueIDConstraintException```)

## Persistence

All records are stored in persistent storage. So when the system restarts one can load previously created wallest and have all functionalities persist. Refer to ```Wallet.deleteAllRecord()``` in the javadoc to learn how to erase all data.

## Concurrent Usage Note

This library is safe for concurrent user - however if multiple threads are changing the contents of a wallet with a given id - one should be aware that the balance as read by a thread might change as other threads make deposits, withdrawals, or transfers. This is similar to a shared account for a married couple! One may think they have $100.00 in the bank but did not realize that their significant other spent $30.00 and went to the bank to withdraw $80.00 to find out that they had insufficient funds.

If one wants to ensure that the balance will not change during an operation, he will need to put a lock on the account and unlock when the operation required is done.

```java
synchronized( wallet.getAccount("savings") ){
  // read the balance of 'savings' and act upon it
}
```
