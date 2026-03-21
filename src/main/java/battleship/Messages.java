package battleship;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Messages {

    private static ResourceBundle bundle;

    public static void escolherIdioma(Scanner in) {
        System.out.println("Escolha o idioma / Choose a language / Elija el idioma:");
        System.out.println("1 - Português");
        System.out.println("2 - English");
        System.out.println("3 - Español");
        System.out.print("> ");

        String opcao = in.next();
        Locale locale;

        switch (opcao) {
            case "2":
                locale = new Locale("en");
                break;
            case "3":
                locale = new Locale("es");
                break;
            default:
                locale = new Locale("pt");
                break;
        }

        bundle = ResourceBundle.getBundle("messages", locale);
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static String get(String key, Object... args) {
        return MessageFormat.format(bundle.getString(key), args);
    }
}