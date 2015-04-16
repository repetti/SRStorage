package org.repetti.srs.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Date: 05/05/15
 *
 * @author repetti
 */
public class SimpleMain {
    public static void main(String[] args) {
        new SimpleFrame();//.setVisible(true);
    }

    private static class SimpleFrame extends JFrame {
        private final JButton buttonChoose;
        private final JButton buttonLoad;
        private final JButton buttonSave;
        private final JTextField fieldPath;
        private final JScrollPane scroll;
        private final JTextArea text;

        public SimpleFrame() throws HeadlessException {
            setTitle("SRS Simple UI");
            this.setLayout(null);
            buttonChoose = new JButton("Choose...");
            buttonSave = new JButton("Save...");
            buttonLoad = new JButton("Load...");
            fieldPath = new JTextField();
            text = new JTextArea();
            text.setEditable(true);
            scroll = new JScrollPane(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

            buttonChoose.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    choose();
                }
            });
            buttonLoad.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    load();
                }
            });
            buttonSave.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    save();
                }
            });

            final int w = 640;
            final int h = 480;
            final int b = 5;
            final int bw = 100;
            final int eh = 25;
            final int fw = w - b * 5 - bw * 3;

            fieldPath.setBounds(b, b, fw, eh);
            buttonChoose.setBounds(b * 2 + fw, b, bw, eh);
            buttonLoad.setBounds(b * 3 + fw + bw, b, bw, eh);
            buttonSave.setBounds(b * 4 + fw + bw * 2, b, bw, eh);
            scroll.setBounds(b, b * 2 + eh, w - b * 2, h - eh - b * 2);

            Container pane = this.getContentPane();
            pane.add(fieldPath);
            pane.add(buttonLoad);
            pane.add(buttonSave);
            pane.add(buttonChoose);
            pane.add(scroll);

            this.pack(); //to get insets of the window
            Insets i = this.getInsets();
//            Insets pi = pane.getInsets();
            System.out.println(i);
//            System.out.println(pi);
//            this.setSize(w + i.left + i.right + pi.left + pi.right, h + i.top + i.bottom + pi.top + pi.bottom);
            this.setSize(w + i.left + i.right, h + i.top + i.bottom);
            this.setResizable(false);

            this.setVisible(true);
            this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }

        private void choose() {
            //https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
            String path = fieldPath.getText();
            JFileChooser d = new JFileChooser(path);
            int res = d.showDialog(this, "Choose");
            if (res == JFileChooser.APPROVE_OPTION) {
                fieldPath.setText(d.getSelectedFile().getAbsolutePath());
            }
        }

        private void load() {
            String pass = getPassword("Load");
            if (pass != null) {
                //TODO
            }
        }

        private void save() {
            String pass = getPassword("Save");
            if (pass != null) {
                //TODO
            }
        }

        private String getPassword(String title) {
            // as described in http://blogger.ziesemer.com/2007/03/java-password-dialog.html
            JLabel label = new JLabel("Please enter your password:");
            JPasswordField passwordField = new JPasswordField();
            int status = JOptionPane.showConfirmDialog(null,
                    new Object[]{label, passwordField}, "Password:",
                    JOptionPane.OK_CANCEL_OPTION);

            if (status == JOptionPane.OK_OPTION) {
                return String.valueOf(passwordField.getPassword());
            }
            return null;
        }
    }
}
