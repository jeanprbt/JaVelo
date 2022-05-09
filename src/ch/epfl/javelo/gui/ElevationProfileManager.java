package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

/**
 * Classe gérant l'interaction et l'affichage avec le profil en long de l'itinéraire.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public final class ElevationProfileManager {

    private final ReadOnlyObjectProperty<ElevationProfile> elevationProfile ;
    private final ReadOnlyDoubleProperty highlightedPosition ;

    private final Insets insets ;

    private final Polygon polygon ;
    private final Line line ;
    private final Path path ;
    private final Group labels ;
    private final Pane pane ;
    private final VBox vBox ;
    private final BorderPane borderPane ;

    private final DoubleProperty mousePositionOnProfile ;
    private final ObjectProperty<Rectangle2D> rectangle ;

    private final ObjectProperty<Transform> worldToScreen;
    private final ObjectProperty<Transform> screenToWorld;

    private static final int[] POS_STEPS =
            { 1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000 };
    private static final int[] ELE_STEPS =
            { 5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000 };
    private static final int MIN_SCREEN_SPACING_HORIZONTAL = 50;
    private static final int MIN_SCREEN_SPACING_VERTICAL = 25;

    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> elevationProfile,
                                   ReadOnlyDoubleProperty highlightedPosition) {


        this.elevationProfile = elevationProfile ;
        this.highlightedPosition = highlightedPosition ;

        //Paddings entre les bords du rectangle contenant le profil et ceux de son panneau parent
        insets = new Insets(10, 10, 20, 40);

        //Nœuds JavaFX servant à la représentation du profil, de la grille, de la ligne et des éléments textuels
        polygon = new Polygon();
        line = new Line();
        path = new Path();
        labels = new Group();
        pane = new Pane(polygon, line, path, labels);
        vBox = new VBox();
        borderPane = new BorderPane(pane, null, null, vBox, null);

        //Rectangle contenant le profil
        rectangle = new SimpleObjectProperty<>();

        /* Propriété contenant la position de la souris le long du profil en mètres, initialisée à -1 pour garantir
        l'invisibilité de la ligne au lancement de la fenêtre si le curseur n'est pas sur le panneau */
        mousePositionOnProfile = new SimpleDoubleProperty(-1);

        /* Transformations affines permettant de passer du système de coordonnées du monde réel à celui de la fenêtre graphique,
        initialisées comme des transformations affines vides afin de pouvoir leur lier la ligne avant même de leur donner une valeur */
        worldToScreen = new SimpleObjectProperty<>(new Affine());
        screenToWorld = new SimpleObjectProperty<>(new Affine());

        //Ajout des identités et feuilles de style au profil, à la grille, aux statistiques et au panneau global
        polygon.setId("profile");
        path.setId("grid");
        vBox.setId("profile_data");
        borderPane.getStylesheets().add("elevation_profile.css");

        //Création des liens entre la ligne, la position de la souris sur le profil et les dimensions du rectangle contenant le profil
        installLine();

        //Installation des listeners
        installListeners();

        //Installation des gestionnaires d'évènement
        installHandlers();

        //TODO virer cette ligne
        updateStats();
    }

    /**
     * Méthode retournant le panneau JavaFX affichant le profil en long de l'itinéraire.
     *
     * @return le panneau affichant le profil en long
     */
    public Pane pane(){
        return borderPane ;
    }

    public ReadOnlyDoubleProperty mousePositionOnProfileProperty() {
        return mousePositionOnProfile ;
    }

    //---------------------------------------------- Private ----------------------------------------------//

    /**
     * Méthode privée permettant de mettre à jour le profil affiché lorsque la taille du panneau ou elevationProfile change
     */
    private void updateProfile() {

        polygon.getPoints().clear();

        //Point du polygone en bas à gauche
        Point2D startPoint = worldToScreen.get().transform(0, elevationProfile.get().minElevation());
        polygon.getPoints().addAll(startPoint.getX(), startPoint.getY());

        //Distance correspondant dans le monde réel à une unité JavaFX sur l'écran.
        double stepWorld = elevationProfile.get().length() / rectangle.get().getWidth();

        //Tous les poins hauts du polygone correspondant à toutes les élévations
        for (double x = 0; x <= rectangle.get().getWidth() ; x++) {
            Point2D pointToAdd = worldToScreen.get().transform(x * stepWorld, elevationProfile.get().elevationAt(x * stepWorld));
            polygon.getPoints().addAll(pointToAdd.getX(), pointToAdd.getY());
        }

        //Point du polygone en bas à droite
        Point2D endPoint = worldToScreen.get().transform(elevationProfile.get().length(), elevationProfile.get().minElevation());
        polygon.getPoints().addAll(endPoint.getX(), endPoint.getY());
    }

    /**
     * Méthode privée permettant de calculer les transformations affines passant des coordonnées du monde réel à celles
     * du panneau graphique et son inverse, appelée à chaque changement de la taille du panneau ou du profil.
     */
    private void updateTransform() {

        //Facteurs de mise à l'échelle
        double scaleXFactor = rectangle.get().getWidth() / elevationProfile.get().length();
        double scaleYFactor = -rectangle.get().getHeight() / (elevationProfile.get().maxElevation() - elevationProfile.get().minElevation());

        //Création d'une instance de Affine et ajout des transformations nécessaires
        Affine affine = new Affine();
        affine.prependTranslation(0, -elevationProfile.get().maxElevation());
        affine.prependScale(scaleXFactor, scaleYFactor);
        affine.prependTranslation(insets.getLeft(), insets.getTop());

        //Traitement de la NonInvertibleTransformException, qui ne doit jamais arriver car la transformation est toujours inversible
        worldToScreen.set(affine);
        try {
            screenToWorld.set(worldToScreen.get().createInverse());
        } catch (NonInvertibleTransformException e) {
            throw new Error(e); // Cas ne devant jamais arriver
        }
    }

    /**
     * Méthode privée permettant de mettre à jour la taille du rectangle contenant le profil lorsque la taille du panneau
     * ou elevationProfile change.
     */
    private void updateRectangle (){
        double rectangleWidth = pane.getWidth() - insets.getRight() - insets.getLeft() ;
        double rectangleHeight = pane.getHeight() - insets.getBottom() - insets.getTop() ;
        rectangle.set(new Rectangle2D(insets.getLeft(), insets.getTop(), rectangleWidth, rectangleHeight));
    }

    /**
     * Méthode privée permettant de mettre à jour la grille lorsque la taille du panneau ou le profil change.
     */
    private void updateGrid() {
        
        path.getElements().clear();
        labels.getChildren().clear();

        //Calcul des espacements minimaux entre les lignes et les colonnes dans le monde réel, en mètres
        int horizontalSpacingWorld = findOptimalSpacing(POS_STEPS, MIN_SCREEN_SPACING_HORIZONTAL, true);
        int verticalSpacingWorld = findOptimalSpacing(ELE_STEPS, MIN_SCREEN_SPACING_VERTICAL, false);

        //Calcul des espacements minimaux entre les lignes et les colonnes à l'écran, en unités JavaFX
        double horizontalSpacingScreen = worldToScreen.get().deltaTransform(horizontalSpacingWorld, 0).getX() ;
        double verticalSpacingScreen = worldToScreen.get().deltaTransform(0, -verticalSpacingWorld).getY() ;

        //Création des colonnes verticales espacées de la distance calculée précédemment et ajout des étiquettes correspondantes
        for (double i = 0; i < rectangle.get().getWidth()/horizontalSpacingScreen; i++) {

            path.getElements().add(new MoveTo(i * horizontalSpacingScreen, 0));
            path.getElements().add(new LineTo(i * horizontalSpacingScreen, rectangle.get().getHeight()));

            Text label = createLabel(String.valueOf((int) i * horizontalSpacingWorld / 1000),
                           "horizontal",
                                     rectangle.get().getHeight(),
                                     VPos.TOP);

            label.setLayoutX(-label.prefWidth(0) / 2 + i * horizontalSpacingScreen);
            labels.getChildren().add(label);
        }

        //Calcul du multiple de l'élévation optimale le plus proche de l'élévation maximale du profil
        double offset = worldToScreen.get().deltaTransform(0, -elevationProfile.get().maxElevation() % verticalSpacingWorld).getY();

        //Création des lignes horizontales espacées de la distance calculée précédemment et ajout des étiquettes correspondantes
        for (double i = 0; i < rectangle.get().getHeight() / verticalSpacingScreen; i++) {

            path.getElements().add(new MoveTo(0, i * verticalSpacingScreen + offset));
            path.getElements().add(new LineTo(rectangle.get().getWidth(), i * verticalSpacingScreen + offset));

            Text label = createLabel(String.valueOf((int) (elevationProfile.get().maxElevation() -
                                    (elevationProfile.get().maxElevation() % verticalSpacingWorld) - i * verticalSpacingWorld)),
                            "vertical",
                            i * verticalSpacingScreen + offset,
                                     VPos.CENTER);

            label.setLayoutX(-label.prefWidth(0) - 2);
            labels.getChildren().add(label);
        }

        //Positionnement de la grille et des étiquettes sur leur panneau parent
        labels.setLayoutX(insets.getLeft());
        labels.setLayoutY(insets.getTop());
        path.setLayoutX(insets.getLeft());
        path.setLayoutY(insets.getTop());
    }

    /**
     * Méthode privée permettant de changer les statistiques affichées sous le profil à chaque changement de ce dernier.
     */
    private void updateStats() {
        vBox.getChildren().clear();
        Text text = new Text(String.format("Longueur : %.1f km" +
                                           "     Montée : %.0f m" +
                                           "     Descente : %.0f m" +
                                           "     Altitude : de %.0f m à %.0f m",
                                            elevationProfile.get().length() / 1000,
                                            elevationProfile.get().totalAscent(),
                                            elevationProfile.get().totalDescent(),
                                            elevationProfile.get().minElevation(),
                                            elevationProfile.get().maxElevation()));
        vBox.getChildren().add(text);
    }

    /**
     * Méthode privée permettant de mettre à jour tous les composants du profil.
     */
    private void update(){
        updateRectangle();
        updateTransform();
        updateProfile();
        updateGrid();
    }

    /**
     * Méthode privée permettant, lors de la mise à jour de la grille, de calculer l'espacement optimal entre
     * deux lignes / colonnes en termes de mètres dans le monde réel pour leur laisser au moins min unités JavaFX
     * d'espace à l'écran.
     *
     * @param spacings le tableau des différents espacements possibles dans le monde réel
     * @param min l'espacement minimum à l'écran entre deux lignes / colonnes, en unités JaaFX
     * @param horizontal à true si l'on cherche l'espacement entre les colonnes, et false si l'on cherche celui des lignes
     * @return l'espacement optimal entre deux lignes / colonnes en termes de mètres dans le monde réel
     */
    private int findOptimalSpacing(int[] spacings, int min, boolean horizontal){
        int stepWorld = spacings[spacings.length - 1];
        for (int i = 0; i < spacings.length - 1; i++) {
            double stepScreen = horizontal ?  worldToScreen.get().deltaTransform(spacings[i], 0).getX() :
                                              worldToScreen.get().deltaTransform(0, -spacings[i]).getY();
            if (stepScreen >= min) {
                stepWorld = spacings[i];
                break ;
            }
        }
        return stepWorld ;
    }

    /**
     * Méthode privée permettant de créer les étiquettes affichant les informations de la grille.
     *
     * @param content la valeur que prend l'étiquette
     * @param styleClass la classe de style à donner à l'étiquette
     * @param yPosition la position verticale à donner à l'étiquette
     * @param vPos le point considéré comme l'origine de l'étiquette
     * @return l'étiquette ainsi créée, stylisée et positionnée
     */
    private Text createLabel(String content, String styleClass, double yPosition, VPos vPos){
        Text label = new Text(content);
        label.textOriginProperty().set(vPos);
        label.setFont(Font.font("Avenir", 10));
        label.getStyleClass().add("grid_label");
        label.getStyleClass().add(styleClass);
        label.setLayoutY(yPosition);
        return label ;
    }

    /**
     * Méthode privée permettant de lier les propriétés de la ligne à celles
     * de la position mise en évidence et du rectangle bleu.
     */
    private void installLine() {
        line.layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> worldToScreen.get().transform(highlightedPosition.get(),0).getX(),
                highlightedPosition, worldToScreen)
        );
        line.startYProperty().bind(Bindings.select(rectangle, "minY"));
        line.endYProperty().bind(Bindings.select(rectangle, "maxY"));
        line.visibleProperty().bind(highlightedPosition.greaterThanOrEqualTo(0));
    }

    /**
     * Méthode privée permettant d'installer des listeners sur le panneau afin de mettre à jour le profil
     * lorsque ses dimensions changent et sur le profil en lui-même afin de tout recréer lorsqu'il change.
     */
    private void installListeners() {

        //Ajout de listener sur la largeur du panneau contenant le profil afin de tout recalculer si cette dernière change
        pane.widthProperty().addListener((o, newS, oldS) -> {
            if(pane.getWidth() >  insets.getLeft() + insets.getRight() && pane.getHeight() > insets.getTop() + insets.getBottom())
                update();
        });

        /* Ajout de listener sur la hauteur du panneau contenant le profil afin de tout recalculer si cette dernière change,
        en testant que la taille du panneau est assez grande pour pouvoir tout réafficher. */
        pane.heightProperty().addListener((o, newS, oldS) -> {
            if(pane.getWidth() > insets.getLeft() + insets.getRight() && pane.getHeight() > insets.getTop() + insets.getBottom())
                update();
        });

        //Ajout d'un listener sur le profil afin de tout recréer lorsque celui-ci est modifié
        elevationProfile.addListener((o, newS, oldS) -> {
            update();
            updateStats();
        });
    }

    /**
     * Méthode privée permettant d'installer les gestionnaires d'évènement pour les actions de la souris sur le rectangle contenant
     * le profil afin d'ajuster la propriété contenant la position de la souris sur le profil en conséquence.
     */
    private void installHandlers() {
        pane.setOnMouseMoved(event -> {
            if(rectangle.get().contains(event.getX(), event.getY()))
                mousePositionOnProfile.set((int) screenToWorld.get().transform(event.getX(), 0).getX());
            else mousePositionOnProfile.set(Double.NaN);
        });

        pane.setOnMouseExited(event -> mousePositionOnProfile.set(Double.NaN));
    }
}
