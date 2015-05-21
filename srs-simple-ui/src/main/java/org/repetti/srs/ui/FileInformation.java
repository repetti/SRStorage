package org.repetti.srs.ui;

import org.jetbrains.annotations.NotNull;

/**
 * Created on 21/05/15.
 *
 * @author repetti
 */
public class FileInformation {
    private final String name;
    private final String password;

    public FileInformation(@NotNull String name, @NotNull String password) {
        this.name = name;
        this.password = password;
    }

    public boolean checkName(@NotNull String name) {
        return this.name.equals(name);
    }

    public boolean checkPassword(@NotNull String password) {
        return this.password.equals(password);
    }
}
