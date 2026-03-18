package battleship;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class BattleshipGUI extends Application {

    private static final int TAMANHO_GRELHA = 10;
    private static final int TAMANHO_CELULA = 30; // Tamanho de cada quadrado em pixeis

    // Variável para guardar o motor do jogo (Backend)
    private Game motorDoJogo;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Batalha Naval dos Descobrimentos");

        // Inicializar a lógica do jogo
        IFleet minhaFrota = Fleet.createRandom();
        motorDoJogo = new Game(minhaFrota);

        // --> INICIALIZAR A BASE DE DADOS NO ARRANQUE
        DatabaseManager.inicializarBaseDeDados();

        // Criar os dois tabuleiros
        GridPane meuMar = criarTabuleiro(true);
        GridPane marAdversario = criarTabuleiro(false);

        // Etiquetas para identificar os tabuleiros
        VBox layoutMeuMar = new VBox(10, new Label("O Meu Mar (Frota)"), meuMar);
        layoutMeuMar.setAlignment(Pos.CENTER);

        VBox layoutMarAdversario = new VBox(10, new Label("Mar do Adversário (Tiros)"), marAdversario);
        layoutMarAdversario.setAlignment(Pos.CENTER);

        // Juntar os dois tabuleiros lado a lado
        HBox layoutPrincipal = new HBox(50, layoutMeuMar, layoutMarAdversario);
        layoutPrincipal.setAlignment(Pos.CENTER);
        layoutPrincipal.setPadding(new Insets(20));

        // Configurar a cena (janela)
        Scene scene = new Scene(layoutPrincipal, 800, 450);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // Método auxiliar para gerar uma grelha 10x10
    private GridPane criarTabuleiro(boolean isMeuMar) {
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true); // Mostra as linhas da grelha

        for (int linha = 0; linha < TAMANHO_GRELHA; linha++) {
            for (int coluna = 0; coluna < TAMANHO_GRELHA; coluna++) {
                // Criar o quadrado para representar a célula (água)
                Rectangle celula = new Rectangle(TAMANHO_CELULA, TAMANHO_CELULA);
                celula.setFill(Color.LIGHTBLUE); // Cor inicial da água
                celula.setStroke(Color.GRAY); // Cor da borda

                // Desenhar a nossa própria frota no ecrã da esquerda
                if (isMeuMar) {
                    IPosition pos = new Position(linha, coluna);
                    if (motorDoJogo.getMyFleet().shipAt(pos) != null) {
                        celula.setFill(Color.DARKGRAY);
                    }
                }

                // Lógica de clique no mar do adversário (ecrã da direita)
                if (!isMeuMar) {
                    final int l = linha;
                    final int c = coluna;

                    celula.setOnMouseClicked(event -> {
                        IPosition pos = new Position(l, c);
                        IGame.ShotResult resultado = motorDoJogo.fireSingleShot(pos, false);

                        // A variável tem de ser declarada AQUI, antes de a usarmos nos 'if'
                        String textoResultado;

                        if (resultado.ship() != null) {
                            celula.setFill(Color.RED);
                            textoResultado = "ACERTO";
                            if (resultado.sunk()) {
                                System.out.println("BOOM! Afundaste um navio!");
                                textoResultado = "AFUNDADO";
                            }
                        } else {
                            celula.setFill(Color.WHITE);
                            textoResultado = "AGUA";
                        }

                        // --> REGISTAR NA BASE DE DADOS
                        DatabaseManager.registarJogada(l, c, textoResultado);

                        // Desativar o clique nesta célula
                        celula.setDisable(true);

                        // Verificar condição de vitória
                        if (motorDoJogo.getRemainingShips() == 0) {
                            motorDoJogo.over();
                            System.out.println("PARABÉNS! Todos os navios foram afundados. Ganhaste o jogo!");
                        }
                    });
                }

                grid.add(celula, coluna, linha);
            }
        }
        return grid;
    }

    public static void main(String[] args) {
        launch(args);
    }
}