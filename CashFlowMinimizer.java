import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class CashFlowMinimizer extends JFrame {

    public CashFlowMinimizer() {
        setTitle("Cash Flow Minimizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new CashFlowPanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CashFlowMinimizer::new);
    }
}

class Transaction {
    String debtor;
    String creditor;
    int amount;

    public Transaction(String debtor, String creditor, int amount) {
        this.debtor = debtor;
        this.creditor = creditor;
        this.amount = amount;
    }
}

class PersonAmount {
    String name;
    int amount;

    public PersonAmount(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }
}

class CashFlowPanel extends JPanel {
    private final int SCREEN_WIDTH = 700;
    private final int SCREEN_HEIGHT = 500;

    private List<Transaction> transactions = new ArrayList<>();
    private JTextField debtorField, creditorField, amountField;
    private List<PersonAmount> netAmountsList = new ArrayList<>();

    public CashFlowPanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(new Color(30, 30, 30));
        setLayout(null);

        JLabel titleLabel = new JLabel("Cash Flow Minimizer (Graph & Min-Heap)");
        titleLabel.setBounds(150, 20, 450, 30);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel);

        JLabel dLabel = new JLabel("Debtor:");
        dLabel.setBounds(50, 80, 60, 25);
        dLabel.setForeground(Color.lightGray);
        add(dLabel);

        debtorField = new JTextField();
        debtorField.setBounds(110, 80, 100, 25);
        add(debtorField);

        JLabel cLabel = new JLabel("Creditor:");
        cLabel.setBounds(230, 80, 60, 25);
        cLabel.setForeground(Color.lightGray);
        add(cLabel);

        creditorField = new JTextField();
        creditorField.setBounds(290, 80, 100, 25);
        add(creditorField);

        JLabel aLabel = new JLabel("Amount:");
        aLabel.setBounds(410, 80, 60, 25);
        aLabel.setForeground(Color.lightGray);
        add(aLabel);

        amountField = new JTextField();
        amountField.setBounds(470, 80, 80, 25);
        add(amountField);

        JButton addButton = new JButton("Add");
        addButton.setBounds(570, 80, 80, 25);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTransaction();
            }
        });
        add(addButton);

        JButton minimizeButton = new JButton("Minimize Cash Flow");
        minimizeButton.setBounds(250, 130, 200, 35);
        minimizeButton.setBackground(new Color(46, 139, 87));
        minimizeButton.setForeground(Color.white);
        minimizeButton.setFont(new Font("Arial", Font.BOLD, 14));
        minimizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateMinimization();
            }
        });
        add(minimizeButton);

        setFocusable(true);
    }

    private void addTransaction() {
        String debtor = debtorField.getText().trim();
        String creditor = creditorField.getText().trim();
        String amountStr = amountField.getText().trim();

        if (!debtor.isEmpty() && !creditor.isEmpty() && !amountStr.isEmpty()) {
            try {
                int amount = Integer.parseInt(amountStr);
                transactions.add(new Transaction(debtor, creditor, amount));
                debtorField.setText("");
                creditorField.setText("");
                amountField.setText("");
                netAmountsList.clear();
                repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.");
            }
        }
    }

    private void calculateMinimization() {
        if (transactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one transaction first.");
            return;
        }

        java.util.Map<String, Integer> balanceMap = new java.util.HashMap<>();
        for (Transaction t : transactions) {
            balanceMap.put(t.debtor, balanceMap.getOrDefault(t.debtor, 0) - t.amount);
            balanceMap.put(t.creditor, balanceMap.getOrDefault(t.creditor, 0) + t.amount);
        }

        PriorityQueue<PersonAmount> minHeap = new PriorityQueue<>(Comparator.comparingInt(p -> p.amount));
        PriorityQueue<PersonAmount> maxHeap = new PriorityQueue<>((p1, p2) -> Integer.compare(p2.amount, p1.amount));

        for (java.util.Map.Entry<String, Integer> entry : balanceMap.entrySet()) {
            if (entry.getValue() < 0) {
                minHeap.add(new PersonAmount(entry.getKey(), entry.getValue()));
            } else if (entry.getValue() > 0) {
                maxHeap.add(new PersonAmount(entry.getKey(), entry.getValue()));
            }
        }

        netAmountsList.clear();
        while (!minHeap.isEmpty() && !maxHeap.isEmpty()) {
            PersonAmount debtor = minHeap.poll();
            PersonAmount creditor = maxHeap.poll();

            int settledAmount = Math.min(-debtor.amount, creditor.amount);
            netAmountsList.add(new TransactionResult(debtor.name, creditor.name, settledAmount));

            debtor.amount += settledAmount;
            creditor.amount -= settledAmount;

            if (debtor.amount < 0) {
                minHeap.add(debtor);
            }
            if (creditor.amount > 0) {
                maxHeap.add(creditor);
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.lightGray);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Entered Transactions:", 50, 190);

        int yOffset = 210;
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i < Math.min(transactions.size(), 6); i++) {
            Transaction t = transactions.get(i);
            g.drawString((i + 1) + ". " + t.debtor + " pays " + t.creditor + " : $" + t.amount, 50, yOffset);
            yOffset += 20;
        }

        g.setColor(Color.cyan);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Optimized Transactions (Min-Heap Result):", 370, 190);

        yOffset = 210;
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i < netAmountsList.size(); i++) {
            PersonAmount res = netAmountsList.get(i);
            TransactionResult tr = (TransactionResult) res;
            g.drawString((i + 1) + ". " + tr.debtor + " -> " + tr.creditor + " : $" + tr.amount, 370, yOffset);
            yOffset += 20;
        }
    }

    private static class TransactionResult extends PersonAmount {
        String debtor;
        String creditor;

        public TransactionResult(String debtor, String creditor, int amount) {
            super(creditor, amount);
            this.debtor = debtor;
            this.creditor = creditor;
        }
    }
}
