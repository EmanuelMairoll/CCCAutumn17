import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Go4IT implements FileParsing.ContentHandler {

    //DES IS HÄSSLICH, I WOAS! OBA I HOB KOAN NERV FÜR DEPENDENCY INJECTION!
    public static List<Transaction> transactions;

    public static void main(String[] args) {
        FileParsing.parseFolder("levels/level3", new Go4IT());
    }

    @Override
    public void handle(String[] params, String filename) {
        int paramIndex = 0;

        int numberOfTransactions = Integer.parseInt(params[paramIndex++]);
        transactions = new ArrayList<>();
        for (int transactionIndex = 0; transactionIndex < numberOfTransactions; transactionIndex++) {
            String line = params[paramIndex++];
            String[] parts = line.split(" ");

            String id = parts[0];
            int numberInputs = Integer.parseInt(parts[1]);
            List<Transaction.InputElement> inputs = new ArrayList<>();
            for (int i = 0; i <numberInputs ; i++) {
                inputs.add(new InputElement())
            }
            long submitTime = Long.parseLong(parts[3]);
        }

        Collections.sort(transactions);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("output/" + filename));

            writer.write(transactions.size() + "\n");
            for (Transaction transaction : transactions) {
                if (transaction.isValid()) {
                    writer.write(transaction.toString() + "\n");
                    System.out.println(transaction.toString());
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static class Transaction implements Comparable<Transaction> {

        private String transactionId;
        private InputElement[] inputs;
        private OutputElement[] outputs;
        private long submitTime;

        public static Transaction byId(String transactionId) {
            for (Transaction transaction : transactions) {
                if (transaction.transactionId.equals(transactionId)) {
                    return transaction;
                }
            }
            return null;
        }

        //REQUIRES LIST BEING SORTED
        public static Transaction byIdBeforeThis(String transactionId, Transaction current) {
            for (Transaction transaction : transactions) {
                if (transaction.equals(current)) {
                    return null;
                }

                if (transaction.transactionId.equals(transactionId)) {
                    return transaction;
                }
            }
            return null;
        }

        //REQUIRES LIST BEING SORTED
        public static List<Transaction> transactionsBeforeThis(Transaction current) {
            List<Transaction> transactionsBefore = new ArrayList<>();
            for (Transaction transaction : transactions) {
                if (transaction.equals(current)) {
                    break;
                }

                transactionsBefore.add(transaction);
            }
            return transactionsBefore;
        }

        @Override
        public int compareTo(Transaction o) {
            return Long.compare(submitTime, o.submitTime);
        }

        public OutputElement getOutputElementForOwner(String owner) {
            for (OutputElement output : outputs) {
                if (output.outputTransactionOwner.equals(owner)) {
                    return output;
                }
            }

            return null;
        }

        public boolean isValid() {
            //SUM CHECK

            int inputSum = 0;
            for (InputElement input : inputs) {
                inputSum += input.inputTransactionAmount;
            }

            int outputSum = 0;
            for (OutputElement output : outputs) {
                outputSum += output.outputTransactionAmount;
            }

            if (inputSum != outputSum) {
                return false;
            }

            //PREVIOUS CHECK / COMPLETE CHECK

            for (InputElement input : inputs) {
                if (!input.inputTransactionOwner.equals("origin")) {
                    Transaction previous = Transaction.byIdBeforeThis(input.previousTransactionId, this);
                    if (previous == null) {
                        return false;
                    } else {
                        OutputElement previousOutputElement = previous.getOutputElementForOwner(input.inputTransactionOwner);
                        if (previousOutputElement.outputTransactionAmount != input.inputTransactionAmount) {
                            return false;
                        }
                    }
                }
            }

            //OWNER OUTPUT CHECK
            List<String> owners = Arrays.stream(outputs).map(outputElement -> outputElement.outputTransactionOwner).collect(Collectors.toList());
            for (int i1 = 0; i1 < owners.size(); i1++) {
                for (int i2 = i1 + 1; i2 < owners.size(); i2++) {
                    if (owners.get(i1).equals(owners.get(i2))) {
                        return false;
                    }
                }
            }

            //MULTIPLE USAGE CHECK
            for (InputElement input : inputs) {
                List<Transaction> previous = Transaction.transactionsBeforeThis(this);
                for (Transaction t : previous) {
                    for (InputElement pI : t.inputs) {
                        if (pI.previousTransactionId.equals(this.transactionId)) {
                            for (InputElement thisInput : this.inputs) {
                                if (thisInput.inputTransactionOwner.equals(pI.inputTransactionOwner)) {
                                    return false;
                                }
                            }
                        }
                    }
                }


            }

            //AMOUNT CHECK
            for (InputElement input : inputs) {
                if (input.inputTransactionAmount <= 0) {
                    return false;
                }
            }

            for (OutputElement output : outputs) {
                if (output.outputTransactionAmount <= 0) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(transactionId);
            sb.append(" ");

            sb.append(inputs.length);
            sb.append(" ");
            for (InputElement input : inputs) {
                sb.append(input.previousTransactionId);
                sb.append(" ");
                sb.append(input.inputTransactionOwner);
                sb.append(" ");
                sb.append(input.inputTransactionAmount);
                sb.append(" ");
            }

            sb.append(outputs.length);
            sb.append(" ");
            for (OutputElement output : outputs) {
                sb.append(output.outputTransactionOwner);
                sb.append(" ");
                sb.append(output.outputTransactionAmount);
                sb.append(" ");
            }

            sb.append(submitTime);


            return sb.toString();
        }

        public static class InputElement {
            public String previousTransactionId;
            public String inputTransactionOwner;
            public int inputTransactionAmount;

            public InputElement(String previousTransactionId, String inputTransactionOwner, int inputTransactionAmount) {
                this.previousTransactionId = previousTransactionId;
                this.inputTransactionOwner = inputTransactionOwner;
                this.inputTransactionAmount = inputTransactionAmount;
            }
        }

        public static class OutputElement {
            public String outputTransactionOwner;
            public int outputTransactionAmount;

            public OutputElement(String outputTransactionOwner, int outputTransactionAmount) {
                this.outputTransactionOwner = outputTransactionOwner;
                this.outputTransactionAmount = outputTransactionAmount;
            }
        }
    }
}

