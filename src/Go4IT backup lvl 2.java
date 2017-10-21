import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Go4IT implements FileParsing.ContentHandler {

    //DES IS HÄSSLICH, I WOAS! OBA I HOB KOAN NERV FÜR DEPENDENCY INJECTION!
    public static List<Account> accounts;
    public static List<Transaction> transactions;

    public static void main(String[] args) {
        FileParsing.parseFolder("levels/level3", new Go4IT());
    }

    public static boolean validateCatBan(String catBan) {
        String code = catBan.substring(0, 3);
        String checksum = catBan.substring(3, 5);
        String id = catBan.substring(5);

        //VALIDATION UPPER-/LOWERCASE

        final int OFFSET_UPPERCASE_A = 65;
        final int OFFSET_LOWERCASE_A = 97;
        final int ALPHABET_LENGTH = 26;
        int[] occurrencesPerChar = new int[256];

        char[] chars = id.toCharArray();
        for (int charsIndex = 0; charsIndex < chars.length; charsIndex++) {
            int asciiValue = (int) chars[charsIndex];
            occurrencesPerChar[asciiValue]++;
        }

        for (int letterIndex = 0; letterIndex < ALPHABET_LENGTH; letterIndex++) {
            int occurrencesUppercase = occurrencesPerChar[OFFSET_UPPERCASE_A + letterIndex];
            int occurrencesLowercase = occurrencesPerChar[OFFSET_LOWERCASE_A + letterIndex];

            if (occurrencesUppercase != occurrencesLowercase) {
                return false;
            }
        }

        //VALIDATION CHECKSUM

        long asciiSum = 0;
        String newString = id + "CAT00";
        char[] newChars = newString.toCharArray();
        for (int charsIndex = 0; charsIndex < newChars.length; charsIndex++) {
            asciiSum += (int) newChars[charsIndex];
        }

        int remainder = (int) (asciiSum % 97);
        int calculatedChecksum = 98 - remainder;

        if (Integer.parseInt(checksum) != calculatedChecksum) {
            return false;
        }

        return true;
    }

    public static Account byCatBan(String catBan) throws RuntimeException {
        for (Account account : accounts) {
            if (account.getCatBan().equals(catBan)) {
                return account;
            }
        }
        return null;
    }

    @Override
    public void handle(String[] params, String filename) {
        int paramIndex = 0;

        int numberOfAccounts = Integer.parseInt(params[paramIndex++]);
        accounts = new ArrayList<>();
        for (int accountIndex = 0; accountIndex < numberOfAccounts; accountIndex++) {
            String line = params[paramIndex++];
            String[] parts = line.split(" ");
            String name = parts[0];
            String catBan = parts[1];
            int balance = Integer.parseInt(parts[2]);
            int overdraftLimit = Integer.parseInt(parts[3]);
            accounts.add(new Account(name, balance, catBan, overdraftLimit));
        }

        int numberOfTransactions = Integer.parseInt(params[paramIndex++]);
        transactions = new ArrayList<>();
        for (int transactionIndex = 0; transactionIndex < numberOfTransactions; transactionIndex++) {
            String line = params[paramIndex++];
            String[] parts = line.split(" ");

            String fromCatBan = parts[0];
            String toCatBan = parts[1];

            String fromUncheckedCatBan = parts[0];
            String toUncheckedCatBan = parts[1];
            int amount = Integer.parseInt(parts[2]);
            long submitTime = Long.parseLong(parts[3]);
            transactions.add(new Transaction(fromUncheckedCatBan, toUncheckedCatBan, amount, submitTime));
        }

        Collections.sort(transactions);

        transactions.forEach(Transaction::perform);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("output/" + filename));

            writer.write(accounts.size() + "\n");
            for (Account account : accounts) {
                if (Go4IT.validateCatBan(account.catBan)){
                    writer.write(account.toString() + "\n");
                    System.out.println(account.toString());
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static class Account {

        private final String name;
        private final String catBan;
        private final int overdraft;

        private int balance;


        public Account(String name, int balance, String catBan, int overdraft) {
            this.name = name;
            this.balance = balance;
            this.catBan = catBan;
            this.overdraft = overdraft;
        }

        public boolean transactionAllowed(int withdraw) {
            return (balance - withdraw) >= -overdraft;
        }

        public void plus(int amount) {
            balance += amount;
        }

        public void minus(int amount) {
            balance -= amount;
        }

        public String getName() {
            return name;
        }

        public int getBalance() {
            return balance;
        }

        public String getCatBan() {
            return catBan;
        }

        @Override
        public String toString() {
            return name + " " + balance;
        }


    }

    public static class Transaction implements Comparable<Transaction> {

        private String fromUncheckedCatBan;
        private String toUncheckedCatBan;
        private int amount;
        private long submitTime;

        public Transaction(String fromUncheckedCatBan, String toUncheckedCatBan, int amount, long submitTime) {
            this.fromUncheckedCatBan = fromUncheckedCatBan;
            this.toUncheckedCatBan = toUncheckedCatBan;
            this.amount = amount;
            this.submitTime = submitTime;
        }

        public boolean perform() {

            Account from = Go4IT.byCatBan(fromUncheckedCatBan);
            if (from == null || !Go4IT.validateCatBan(from.catBan)){
                return false;
            }

            Account to = Go4IT.byCatBan(toUncheckedCatBan);
            if (to == null || !Go4IT.validateCatBan(to.catBan)){
                return false;
            }

            if (!from.transactionAllowed(amount)) {
                return false;
            }

            from.minus(amount);
            to.plus(amount);

            return true;

        }

        @Override
        public int compareTo(Transaction o) {
            return Long.compare(submitTime, o.submitTime);
        }

        public int getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return fromUncheckedCatBan + " " + toUncheckedCatBan + " " + amount + " " + submitTime;

        }
    }

}
