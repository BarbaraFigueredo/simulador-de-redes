package com.netsim.presentation;

/**
 * Launcher separado do Application para compatibilidade com fat JARs.
 * O classloader de alguns runtimes não permite iniciar diretamente
 * uma subclasse de javafx.application.Application.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}