/**
 * 
 */
package battleship;

import java.util.Scanner;

/**
 * The type Main.
 *
 * @author britoeabreu
 * @author adrianolopes
 * @author miguelgoulao
 */
public class Main
{
	/**
	 * Main.
	 *
	 * @param args the args
	 */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Messages.escolherIdioma(in);
        System.out.println(Messages.get("menu.titulo"));
        Tasks.menu(in);
    }
}
