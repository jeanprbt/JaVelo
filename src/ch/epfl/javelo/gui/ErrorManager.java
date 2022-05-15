package ch.epfl.javelo.gui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Classe gérant l'affichage des messages d'erreur.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class ErrorManager {

    private final StackPane pane ;
    private final VBox vBox ;

    private SequentialTransition sequentialTransition ;

    public ErrorManager(){
        sequentialTransition = new SequentialTransition();

        vBox = new VBox();
        vBox.getStylesheets().add("error.css");

        //Utilisation d'un StackPane afin que les propriétés CSS s'appliquent correctement sur la vBox
        pane = new StackPane(vBox);
        pane.setMouseTransparent(true);
    }

    /**
     * Méthode retournant le panneau JavaFX affichant temporairement les messages d'erreur.
     *
     * @return le panneau affichant les messages d'erreurs
     */
    public Pane pane(){
        return pane ;
    }

    /**
     * Méthode permettant de temporairement faire apparaître à l'écran un court message d'erreur
     * accompagné d'un bip sonore.
     *
     * @param message le message d'erreur à afficher
     */
    public void displayError(String message){

        //Ajout du texte au panneau
        vBox.getChildren().clear();
        vBox.getChildren().add(new Text(message));

        //Lancement de la transition et du bip sonore
        displayTransitionsAndBeep(vBox);
    }

    //---------------------------------------------- Private ----------------------------------------------//

    /**
     * Méthode privée permettant d'afficher à l'écran une animation sur le panneau contenant le message d'erreur
     * et de diffuser un bip sonore lorsqu'une erreur est signalée.
     *
     * @param node le nœud auquel appliquer les transitions
     */
    private void displayTransitionsAndBeep(Node node){

        //Lancement du bip sonore
        java.awt.Toolkit.getDefaultToolkit().beep();

        //Première transition : augmenter l'opacité du panneau pour le rendre visible
        FadeTransition fadeTransition1 = new FadeTransition(Duration.seconds(0.2), node);
        fadeTransition1.setFromValue(0);
        fadeTransition1.setToValue(0.8);

        //Seconde transition : diminuer l'opacité du panneau pour à nouveau le rendre invisible
        FadeTransition fadeTransition2 = new FadeTransition(Duration.seconds(0.5), node);
        fadeTransition2.setFromValue(0.8);
        fadeTransition2.setToValue(0);

        //Transition intermédiaire : laisser le panneau visible durant 2 secondes
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(2));

        //Si une animation est lancée alors qu'une autre est déjà en cours, interruption de cette dernière
        if(sequentialTransition.statusProperty().get() == Animation.Status.RUNNING){
            sequentialTransition.stop();
        }

        //Lancement de l'animation
        sequentialTransition = new SequentialTransition(fadeTransition1, pauseTransition, fadeTransition2);
        sequentialTransition.play();
    }
}
