# uwallet

## Docs
[The javadocs for the uwallet package](http://htmlpreview.github.io/?https://github.com/zdalih/uwallet/blob/master/javadoc/uwallet/Wallet.html)

## Usage

The main user interface is the ```uwallet.Wallet``` class. a ```Wallet``` object can be constructed given what must be a unique id and a region code. A wallet is a collection of 'accounts' which are entities that hold balances and can be deposited to / withdrawn from.

The unique id is used to tell one wallet apart from another - and to be able to load wallets from record. When creating wallets ensure that you use a unique id that is suitable for your case. For example a user id if the user is to only have a single wallet.

The region code will be used to format balances within the wallet to the respective currency norms in such region. This region code is a [ISO 3166 alpha-2 country code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2) or [UN M.49 numeric-3 area code](https://en.wikipedia.org/wiki/UN_M.49).

```java
Wallet wallet = new Wallet("ID001", "US");
```
Creation of a wallet will create a record in the persistent data. This allows one to get a ```Wallet``` object by it's id.

```java
Wallet wallet = Wallet.loadWallet("ID001")
```

### Wallet: Creating Accounts
Now we may start adding new accounts to this wallet. Accounts are identified again by a name -  a wallet can not have two accounts with the same name. 

```java
wallet.createNewAccount("chequing");
wallet.createNewAccount("savings");
```

We can now call on to one of the following mechanisms onto accounts:
* wallet.depositTo("chequing")
* wallet.withdrawFrom("chequing") - will not allow the account's balance to drop below 0.
* wallet.transfer("chequing", "savings") - will not allow the 'from' account's balance to drop below 0.

### Wallet: Getting Last N Transactions

One can fetch the last N transactions for a given account by call to the followinig method which in this acse will return a ```List<Transaction>``` of the past 10 transactions in the account 'chequing'.

```java
wallet.getLastNTransactions("chequing", 10);
```

The [Transaction](https://htmlpreview.github.io/?https://raw.githubusercontent.com/zdalih/uwallet/master/javadoc/uwallet/Transaction.html) is an immutable object that contains a globally unique identifier, the amount of the transaction, the nature of the transaction, and a description of the transaction if one exists.
