package org.repetti.srs.ui;

import org.repetti.srs.core.ParametrizedCoder;
import org.repetti.utils.ExceptionHelper;
import org.repetti.utils.LoggerHelperSlf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Date: 05/05/15
 *
 * @author repetti
 */
public class SimpleMain {
    private static final Logger log = LoggerFactory.getLogger(SimpleMain.class);
    private static final int keyLength = 256;
    private static final int iterationCount = 100;
    private static final String algorithm = "AES/CTR/PKCS7Padding";
    private final ParametrizedCoder coder = new ParametrizedCoder();

    public static void main(String[] args) {
        LoggerHelperSlf4j.setDebug();
        new SimpleMain().start();
        log.debug("initialized");
    }

    private void start() {
        new SimpleFrame().setVisible(true);
        log.debug("Window shown");
    }

    private class SimpleFrame extends JFrame {
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
            File file = new File(fieldPath.getText());
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this,
                        "File not found.",
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!file.isFile()) {
                JOptionPane.showMessageDialog(this,
                        "Not a regular file.",
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!file.canRead()) {
                JOptionPane.showMessageDialog(this,
                        "Unable to read from file.",
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            byte[] res;
            try {
                Path path = file.toPath();
                res = Files.readAllBytes(path);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Unable to read from File: " + ExceptionHelper.stackTraceToString(e),
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String pass = getPassword("Load");
            if (pass != null) {
                try {

                    byte[] ret = coder.decode(res, pass.toCharArray(), algorithm, keyLength, iterationCount);
                    String retText = new String(ret);
//                    System.out.println(retText + " " + new String(textOriginal));
                    this.text.setText(retText);

                    JOptionPane.showMessageDialog(this,
                            res.length + " bytes successfully wrote.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            ExceptionHelper.stackTraceToString(e),
                            "Exception",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot be emty. Aborting.",
                        "Invalid password",
                        JOptionPane.WARNING_MESSAGE);
            }
        }

        private void save() {
            File file = new File(fieldPath.getText());
            if (file.exists() && !file.canWrite()) {
                JOptionPane.showMessageDialog(this,
                        "Unable to write to file.",
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String pass = getPassword("Save");
            if (pass != null) {
                try {
                    String text = this.text.getText();

                    byte[] textOriginal = text.getBytes();

                    byte[] res = coder.encode(textOriginal, pass.toCharArray(), algorithm, keyLength, iterationCount);

                    file.createNewFile();
                    BufferedOutputStream bos = null;
                    try {
                        try {
                            bos = new BufferedOutputStream(new FileOutputStream(file));
                            bos.write(res);
//                            bos.clo
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(this,
                                    "Unable to write to File: " + ExceptionHelper.stackTraceToString(e),
                                    "Error",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    } finally {
                        if (bos != null) {
                            try {
                                bos.close();
                            } catch (IOException e) {
                                log.warn("Exception while closing file {}", file, e);
                            }
                        }
                    }

                    JOptionPane.showMessageDialog(this,
                            res.length + " bytes successfully wrote.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            ExceptionHelper.stackTraceToString(e),
                            "Exception",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot be emty. Aborting.",
                        "Invalid password",
                        JOptionPane.WARNING_MESSAGE);
            }
        }

        private String getPassword(String title) {
            // as described in http://blogger.ziesemer.com/2007/03/java-password-dialog.html
            JLabel label = new JLabel("Please enter your password:");
            JPasswordField passwordField = new JPasswordField();
            int status = JOptionPane.showConfirmDialog(null,
                    new Object[]{label, passwordField}, title,
                    JOptionPane.OK_CANCEL_OPTION);

            if (status == JOptionPane.OK_OPTION) {
                return String.valueOf(passwordField.getPassword());
            }
            return null;
        }
    }
}
