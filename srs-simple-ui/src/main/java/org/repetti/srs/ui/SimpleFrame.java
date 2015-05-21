package org.repetti.srs.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.repetti.srs.core.ParametrizedCoder;
import org.repetti.utils.ExceptionHelper;
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
 * Created on 21/05/15.
 *
 * @author repetti
 */
public class SimpleFrame extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(SimpleFrame.class);
    private final ParametrizedCoder coder = new ParametrizedCoder();
    private final JButton buttonChoose;

    //    private static final int keyLength = 256;
//    private static final int iterationCount = 100;
//    private static final String algorithm = "AES/CTR/PKCS7Padding";
    private final JButton buttonLoad;
    private final JButton buttonSave;
    private final JTextField fieldPath;
    private final JScrollPane scroll;
    private final JTextArea text;
    private volatile FileInformation fileInformation;

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
        final int bw = 120;
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
        log.debug("{}", i);
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

                byte[] ret = coder.decode(res, pass.toCharArray());
                String retText = new String(ret);
//                    System.out.println(retText + " " + new String(textOriginal));
                this.text.setText(retText);
                updateFileInformation(file, pass);
                JOptionPane.showMessageDialog(this,
                        "Successfully read.",
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
                    "Cannot be empty. Aborting.",
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
        boolean checkPassword = checkName(file);
        FileInformation oldFileInformation = fileInformation;
        String pass;
        while (true) {
            pass = getPassword(checkPassword ? "Save: reenter password" : "Save: define password");
            if (checkPassword) {
                if (pass == null) {
                    int res = JOptionPane.showConfirmDialog(this, "Define new password?", "Question", JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        checkPassword = false;
                        removeFileInformation();
                    } else {
                        return;
                    }
                } else {
                    if (checkPassword(pass)) {
                        break;
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Passwords don't match. Please define new password.",
                                "Invalid password",
                                JOptionPane.WARNING_MESSAGE);
                        checkPassword = false;
                        fileInformation = oldFileInformation;
                    }

                }
            } else {
                if (pass == null) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot be empty. Aborting.",
                            "Invalid password",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                } else {
                    checkPassword = true;
                    updateFileInformation(file, pass);
                }
            }
        }
        try {
            String text = this.text.getText();

            byte[] textOriginal = text.getBytes();

            byte[] res = coder.encode(textOriginal, pass.toCharArray());

            if (file.createNewFile()) {
                log.debug("File created");
            } else {
                log.debug("File already existed");
            }
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

    }

    @Nullable
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

    /**
     * If the file was read or written before, we should check that the password is the same.
     * If this is the new file, we should ask to enter password twice.
     *
     * @param file current file
     * @return true if this is the last accessed file
     */
    private boolean checkName(@NotNull File file) {
        return fileInformation != null && fileInformation.checkName(file.getAbsolutePath());
    }

    /**
     * Checks if the password was reentered correctly
     *
     * @param password password to check
     * @return true if password is same
     */
    private boolean checkPassword(@NotNull String password) {
        assert fileInformation != null;
        return fileInformation.checkPassword(password);
    }

    private void removeFileInformation() {
        this.fileInformation = null;
    }

    private void updateFileInformation(@NotNull File file, @NotNull String password) {
        this.fileInformation = new FileInformation(file.getAbsolutePath(), password);
    }
}
