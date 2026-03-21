package battleship;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

public class Tasks {

    private static final Logger LOGGER = LogManager.getLogger();
    private static List<String> historico = new ArrayList<>();

    private static final String AJUDA = "ajuda";
    private static final String GERAFROTA = "gerafrota";
    private static final String LEFROTA = "lefrota";
    private static final String DESISTIR = "desisto";
    private static final String RAJADA = "rajada";
    private static final String TIROS = "tiros";
    private static final String MAPA = "mapa";
    private static final String STATUS = "estado";
    private static final String SIMULA = "simula";

    public static void menu(Scanner in) {

        IFleet myFleet = null;
        IGame game = null;
        menuHelp();

        System.out.print("> ");
        String command = in.next();
        while (!command.equals(DESISTIR)) {

            switch (command) {
                case GERAFROTA:
                    myFleet = Fleet.createRandom();
                    game = new Game(myFleet);
                    game.printMyBoard(false, true);
                    break;
                case LEFROTA:
                    myFleet = buildFleet(in);
                    game = new Game(myFleet);
                    game.printMyBoard(false, true);
                    break;
                case STATUS:
                    if (myFleet != null)
                        myFleet.printStatus();
                    break;
                case MAPA:
                    if (myFleet != null)
                        game.printMyBoard(false, true);
                    break;
                case RAJADA:
                    if (game != null) {

                        System.out.println(Messages.get("menu.prompt"));

                        DateTime inicio = new DateTime();

                        String t1 = in.next();
                        String t2 = in.next();
                        String t3 = in.next();

                        DateTime fim = new DateTime();
                        int segundos = Seconds.secondsBetween(inicio, fim).getSeconds();

                        game.readEnemyFire(new Scanner(t1 + " " + t2 + " " + t3));

                        int minutos = segundos / 60;
                        int secsRestantes = segundos % 60;
                        String tempoFormatado = String.format("%02d:%02d", minutos, secsRestantes);

                        System.out.println();
                        System.out.println("+---------------------------------+");
                        System.out.println("|  " + Messages.get("jogo.tempo", tempoFormatado) + "        |");
                        System.out.println("+---------------------------------+");

                        String registo = "Rajada: [" + t1 + ", " + t2 + ", " + t3 + "] | Tempo: " + tempoFormatado;
                        historico.add(registo);
                        historico.add("Resultado -> Acertos: " + game.getHits() + " | Restantes: " + game.getRemainingShips());

                        myFleet.printStatus();
                        game.printMyBoard(true, false);
                    }
                    break;
                case SIMULA:
                    if (game != null) {
                        while (game.getRemainingShips() > 0) {
                            game.randomEnemyFire();
                            myFleet.printStatus();
                            game.printMyBoard(true, false);
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        if (game.getRemainingShips() == 0) {
                            game.over();
                            System.exit(0);
                        }
                    }
                    break;
                case TIROS:
                    if (game != null)
                        game.printMyBoard(true, true);
                    break;
                case AJUDA:
                    menuHelp();
                    break;
                default:
                    System.out.println(Messages.get("menu.comando.invalido"));
            }
            System.out.print("> ");
            command = in.next();
        }
        System.out.println(Messages.get("menu.adeus"));
    }

    public static void menuHelp() {
        System.out.println(Messages.get("menu.separador"));
        System.out.println(Messages.get("menu.cabecalho"));
        System.out.println(Messages.get("menu.ajuda"));
        System.out.println("- " + GERAFROTA + ": " + Messages.get("menu.gerafrota"));
        System.out.println("- " + LEFROTA + ": " + Messages.get("menu.lefrota"));
        System.out.println("- " + STATUS + ": " + Messages.get("menu.estado"));
        System.out.println("- " + MAPA + ": " + Messages.get("menu.mapa"));
        System.out.println("- " + RAJADA + ": " + Messages.get("menu.rajada"));
        System.out.println("- " + SIMULA + ": " + Messages.get("menu.simula"));
        System.out.println("- " + TIROS + ": " + Messages.get("menu.tiros"));
        System.out.println("- " + DESISTIR + ": " + Messages.get("menu.desistir"));
        System.out.println(Messages.get("menu.separador"));
    }

    public static Fleet buildFleet(Scanner in) {
        assert in != null;
        Fleet fleet = new Fleet();
        int i = 0;
        while (i < Fleet.FLEET_SIZE) {
            IShip s = readShip(in);
            if (s != null) {
                boolean success = fleet.addShip(s);
                if (success)
                    i++;
                else
                    LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
            } else {
                LOGGER.info("Navio desconhecido!");
            }
        }
        LOGGER.info("{} navios adicionados com sucesso!", i);
        return fleet;
    }

    public static Ship readShip(Scanner in) {
        assert in != null;
        String shipKind = in.next();
        Position pos = readPosition(in);
        char c = in.next().charAt(0);
        Compass bearing = Compass.charToCompass(c);
        return Ship.buildShip(shipKind, bearing, pos);
    }

    public static Position readPosition(Scanner in) {
        assert in != null;
        int row = in.nextInt();
        int column = in.nextInt();
        return new Position(row, column);
    }

    public static IPosition readClassicPosition(@NotNull Scanner in) {
        if (!in.hasNext()) {
            throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
        }
        String part1 = in.next();
        String part2 = null;
        if (in.hasNextInt()) {
            part2 = in.next();
        }
        String input = (part2 != null) ? part1 + part2 : part1;
        input = input.toUpperCase();
        if (input.matches("[A-Z]\\d+")) {
            char column = input.charAt(0);
            int row = Integer.parseInt(input.substring(1));
            return new Position(column, row);
        } else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
            char column = part1.charAt(0);
            int row = Integer.parseInt(part2);
            return new Position(column, row);
        } else {
            throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
        }
    }
}