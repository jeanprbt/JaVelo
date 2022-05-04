package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
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

    private final Polygon polygon ;
    private final Pane pane ;
    private final Line line ;
    private final Path path ;
    private final Group labels ;
    private final Pane borderPane ;

    private final ReadOnlyObjectProperty<ElevationProfile> elevationProfile ;
    private final ReadOnlyDoubleProperty highlightedPosition ;

    private final Insets insets ;

    private final ChangeListener<Rectangle2D> listener ;
    private final ObjectProperty<Rectangle2D> rectangle ;

    private ObjectProperty<Transform> worldToScreen;
    private ObjectProperty<Transform> screenToWorld;

    private static final int[] POS_STEPS =
            { 1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000 };
    private static final int[] ELE_STEPS =
            { 5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000 };


    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> elevationProfile,
                                   ReadOnlyDoubleProperty highlightedPosition) throws NonInvertibleTransformException {

        this.polygon = new Polygon();
        this.line = new Line();
        this.path = new Path();
        this.labels = new Group();
        this.pane = new Pane(polygon, line, path, labels);
        this.borderPane = new BorderPane(pane);
        this.rectangle = new SimpleObjectProperty<>();
        this.insets = new Insets(10, 10, 20, 40);

        this.elevationProfile = elevationProfile ;
        this.highlightedPosition = highlightedPosition ;

        this.listener = (o, newS, oldS) -> setUpLine();
        this.rectangle.addListener(listener);

        this.pane.widthProperty().addListener((o, newS, oldS) -> {
            if(pane.getWidth() >= insets.getLeft() + insets.getRight() && pane.getHeight() >= insets.getTop() + insets.getBottom()) {
                resizeRectangle();
                computeTransform();
                recreateProfile();
                recreateGrid();
            }
        });

        this.pane.heightProperty().addListener((o, newS, oldS) -> {
            if(pane.getWidth() >= insets.getLeft() + insets.getRight() && pane.getHeight() >= insets.getTop() + insets.getBottom()) {
                resizeRectangle();
                computeTransform();
                recreateProfile();
                recreateGrid();
            }
        });

        polygon.setId("profile");
        path.setId("grid");
        borderPane.getStylesheets().add("elevation_profile.css");
    }

    /**
     * Méthode retournant le panneau JavaFX affichant le profil en long de l'itinéraire.
     *
     * @return le panneau affichant le profil en long
     */
    public Pane pane(){
        return borderPane ;
    }

    public ReadOnlyIntegerProperty mousePositionOnProfileProperty() {
        return null;
    }

    //---------------------------------------------- Private ----------------------------------------------//

    private void recreateProfile() {
        polygon.getPoints().clear();

        Point2D startPoint = worldToScreen.get().transform(0, elevationProfile.get().minElevation());
        polygon.getPoints().addAll(startPoint.getX(), startPoint.getY());

        double rate = elevationProfile.get().length() / rectangle.get().getWidth();
        for (double x = 0; x < elevationProfile.get().length() ; x += rate) {
            Point2D pointToAdd = worldToScreen.get().transform(x, elevationProfile.get().elevationAt(x));
            polygon.getPoints().addAll((double) Math.round(pointToAdd.getX()), pointToAdd.getY());
        }

        Point2D endPoint = worldToScreen.get().transform(elevationProfile.get().length(), elevationProfile.get().minElevation());
        polygon.getPoints().addAll(endPoint.getX(), endPoint.getY());
    }

    /**
     * Méthode privée permettant de calculer la transformation affine passant des coordonnées du monde réel au panneau graphique,
     * appelée à chaque changement de la taille du panneau.
     */
    private void computeTransform() {

        double scaleXFactor = rectangle.get().getWidth() / elevationProfile.get().length();
        double scaleYFactor = - rectangle.get().getHeight() / (elevationProfile.get().maxElevation() - elevationProfile.get().minElevation());

        Affine affine = new Affine();
        affine.prependTranslation(0, -elevationProfile.get().maxElevation());
        affine.prependScale(scaleXFactor, scaleYFactor);
        affine.prependTranslation(insets.getLeft(), insets.getTop());

        try {
            worldToScreen = new SimpleObjectProperty<>(affine);
            screenToWorld = new SimpleObjectProperty<>(worldToScreen.get().createInverse());
        } catch (NonInvertibleTransformException e) {
            throw new Error(e); // Cas ne devant jamais arriver
        }
    }

    /**
     * Méthode privée permettant de mettre à jour la taille du rectangle contenant le profil lorsque la taille du panneau change.
     */
    private void resizeRectangle (){
        double rectangleWidth = pane.getWidth() - insets.getRight() - insets.getLeft() ;
        double rectangleHeight = pane.getHeight() - insets.getBottom() - insets.getTop() ;
        rectangle.set(new Rectangle2D(insets.getLeft(), insets.getTop(), rectangleWidth, rectangleHeight));
    }

    /**
     * Méthode privée permettant de mettre à jour la grille.
     */
    private void recreateGrid() {

        path.getElements().clear();

        int posStepWorld = POS_STEPS[POS_STEPS.length - 1];
        int eleStepWorld = ELE_STEPS[ELE_STEPS.length - 1];

        for (int i = 0; i < POS_STEPS.length - 1; i++) {
            double posStepScreen = POS_STEPS[i] * rectangle.get().getWidth() / elevationProfile.get().length();
            if (posStepScreen >= 50){
                posStepWorld = POS_STEPS[i];
                break;
            }
        }

        for (int i = 0; i < ELE_STEPS.length - 1; i++) {
            double eleStepScreen = ELE_STEPS[i] * rectangle.get().getHeight() / (elevationProfile.get().maxElevation() - elevationProfile.get().minElevation());
            if (eleStepScreen >= 25){
                eleStepWorld = ELE_STEPS[i];
                break;
            }
        }

        Point2D stepsScreen = worldToScreen.get().deltaTransform(posStepWorld, -eleStepWorld);
        double posStepScreen = stepsScreen.getX();
        double eleStepScreen = stepsScreen.getY() ;

        for (double i = 0; i < rectangle.get().getWidth(); i+= posStepScreen) {
            path.getElements().add(new MoveTo(i, 0));
            path.getElements().add(new LineTo(i, rectangle.get().getHeight()));
            Text label = new Text(String.valueOf(screenToWorld.get().transform(i, 0).getX()/1000));
            label.textOriginProperty().set(VPos.TOP);
            label.setLayoutX(-label.prefWidth(0) / 2);
            labels.getChildren().add(label);

        }

        double start = (elevationProfile.get().maxElevation() % eleStepWorld) *
                        rectangle.get().getHeight() /
                        (elevationProfile.get().maxElevation() - elevationProfile.get().minElevation());
        for (double i = start; i < rectangle.get().getHeight() ; i += eleStepScreen) {
            path.getElements().add(new MoveTo(0, i));
            path.getElements().add(new LineTo(rectangle.get().getWidth(), i));
            Text label = new Text();
            label.textOriginProperty().set(VPos.CENTER);
            label.setLayoutX(i + label.getWrappingWidth() + 2);
            labels.getChildren().add(label);
        }

        path.setLayoutX(insets.getLeft());
        path.setLayoutY(insets.getTop());




    }

    /**
     * Méthode privée permettant de lier les propriétés de la ligne à celle de la position mise en évidence et
     * du rectangle bleu, puis permettant de retirer le listener l'appelant de la liste des listeners du rectangle
     * une fois ces réglages effectués.
     */
    private void setUpLine() {
        line.layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> highlightedPosition.get() * (rectangle.get().getWidth() / elevationProfile.get().length()) + insets.getLeft(),
                highlightedPosition, rectangle)
        );
        line.startYProperty().bind(Bindings.select(rectangle, "minY"));
        line.endYProperty().bind(Bindings.select(rectangle, "maxY"));
        line.visibleProperty().bind(highlightedPosition.greaterThanOrEqualTo(0));
        rectangle.removeListener(listener);
    }
}
