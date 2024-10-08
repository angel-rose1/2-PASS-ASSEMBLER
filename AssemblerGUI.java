import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class AssemblerGUI extends JFrame {
    private JTextArea sourceCodeArea;
    private JTextArea pass1OutputArea;
    private JTextArea pass2OutputArea;
    private JButton pass1Button, pass2Button;

    private Map<String, Integer> symbolTable = new HashMap<>();
    private Map<String, String> opcodeTable = new HashMap<>();
    private int locctr = 0, start = 0, length = 0;

    public AssemblerGUI() {
        setTitle("Simple Assembler");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Text areas for input and output
        sourceCodeArea = new JTextArea();
        pass1OutputArea = new JTextArea();
        pass2OutputArea = new JTextArea();

        pass1OutputArea.setEditable(false);
        pass2OutputArea.setEditable(false);

        pass1Button = new JButton("Run Pass 1");
        pass2Button = new JButton("Run Pass 2");

        // Create scroll panes for all text areas
        JScrollPane sourceScroll = new JScrollPane(sourceCodeArea);
        JScrollPane pass1Scroll = new JScrollPane(pass1OutputArea);
        JScrollPane pass2Scroll = new JScrollPane(pass2OutputArea);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        buttonPanel.add(pass1Button);
        buttonPanel.add(pass2Button);

        // Panel to contain source input and output areas
        JPanel inputOutputPanel = new JPanel();
        inputOutputPanel.setLayout(new GridLayout(3, 1));
        inputOutputPanel.add(sourceScroll); // Top: Source code input
        inputOutputPanel.add(pass1Scroll);  // Middle: Pass 1 output
        inputOutputPanel.add(pass2Scroll);  // Bottom: Pass 2 output

        add(buttonPanel, BorderLayout.NORTH); // Buttons at top
        add(inputOutputPanel, BorderLayout.CENTER); // Text areas in center

        // Initialize opcode table for Pass 2
        initializeOpcodeTable();

        // Button Actions
        pass1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runPass1();
            }
        });

        pass2Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runPass2();
            }
        });
    }

    // Pass 1: Simulate the generation of the symbol table and intermediate file
    private void runPass1() {
        symbolTable.clear();
        locctr = 0;
        pass1OutputArea.setText("");  // Clear previous Pass 1 output

        String[] lines = sourceCodeArea.getText().split("\n");

        // Read first line to check for START directive
        String[] firstLine = lines[0].trim().split("\\s+");
        if (firstLine.length >= 3 && firstLine[1].equals("START")) {
            start = Integer.parseInt(firstLine[2]);
            locctr = start;
            pass1OutputArea.append("\t" + firstLine[0] + "\t" + firstLine[1] + "\t" + firstLine[2] + "\n");
        } else {
            locctr = 0;
        }

        // Process remaining lines
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String label = parts.length > 2 ? parts[0] : "**";
            String opcode = parts.length > 2 ? parts[1] : parts[0];
            String operand = parts.length > 2 ? parts[2] : parts[1];

            // Output intermediate format
            pass1OutputArea.append(locctr + "\t" + label + "\t" + opcode + "\t" + operand + "\n");

            // If label exists and is not '**', add it to the symbol table
            if (!label.equals("**")) {
                symbolTable.put(label, locctr);
            }

            // Update location counter based on opcode
            if (opcode.equals("WORD")) {
                locctr += 3;
            } else if (opcode.equals("RESW")) {
                locctr += (3 * Integer.parseInt(operand));
            } else if (opcode.equals("RESB")) {
                locctr += Integer.parseInt(operand);
            } else if (opcode.equals("BYTE")) {
                locctr += 1;
            } else {
                locctr += 3; // Default increment for instructions
            }

            // Check for END directive
            if (opcode.equals("END")) {
                break;
            }
        }

        // Display Symbol Table
        pass1OutputArea.append("\nSymbol Table:\n");
        for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
            pass1OutputArea.append(entry.getKey() + "\t" + entry.getValue() + "\n");
        }

        // Calculate length of the program
        length = locctr - start;
        pass1OutputArea.append("\nLength of the program: " + length + "\n");
    }

    // Pass 2: Generate object code using the symbol table and intermediate code
    private void runPass2() {
        pass2OutputArea.setText("");  // Clear previous Pass 2 output

        String[] lines = pass1OutputArea.getText().split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("Symbol") || line.startsWith("Length")) {
                continue;  // Skip empty lines and header sections
            }

            String[] parts = line.trim().split("\\s+");
            if (parts.length < 4) continue;

            String address = parts[0];
            String label = parts[1];
            String opcode = parts[2];
            String operand = parts[3];

            // Generate object code
            if (opcodeTable.containsKey(opcode)) {
                String objectCode = opcodeTable.get(opcode);
                if (symbolTable.containsKey(operand)) {
                    objectCode += String.format("%03d", symbolTable.get(operand));
                } else if (opcode.equals("BYTE")) {
                    if (operand.startsWith("C'")) {
                        objectCode = operand.substring(2, operand.length() - 1);  // Extract characters
                    }
                } else if (opcode.equals("WORD")) {
                    objectCode = String.format("%06X", Integer.parseInt(operand));
                }
                pass2OutputArea.append(address + "\t" + objectCode + "\n");
            }
        }
    }

    // Initialize opcode table
    private void initializeOpcodeTable() {
        opcodeTable.put("LDA", "03");
        opcodeTable.put("STA", "0F");
        opcodeTable.put("LDCH", "53");
        opcodeTable.put("STCH", "57");
    
        // Additional opcodes
        opcodeTable.put("ADD", "18");
        opcodeTable.put("SUB", "1C");
        opcodeTable.put("MUL", "20");
        opcodeTable.put("DIV", "24");
        opcodeTable.put("COMP", "28");
        opcodeTable.put("J", "3C");
        opcodeTable.put("JEQ", "30");
        opcodeTable.put("JGT", "34");
        opcodeTable.put("JLT", "38");
        opcodeTable.put("JSUB", "48");
        opcodeTable.put("RSUB", "4C");
        opcodeTable.put("TIX", "2C");
        opcodeTable.put("AND", "40");
        opcodeTable.put("OR", "44");
        opcodeTable.put("LDX", "04");
        opcodeTable.put("STX", "10");
        opcodeTable.put("TD", "E0");
        opcodeTable.put("RD", "D8");
        opcodeTable.put("WD", "DC");
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AssemblerGUI assemblerGUI = new AssemblerGUI();
            assemblerGUI.setVisible(true);
        });
    }
}
