package neurevolve;

import neurevolve.ui.NewWorldDialog;

/**
 * Neurevolve simulates natural evolution by tracking the development of complex behaviours in
 * simple organisms in a resource-limited world. The behaviour of organisms is controlled by simple
 * neural networks which are, in turn, created by recipes. The recipes are copied during replication
 * with random transcription errors introduced.
 *
 * The user interface for Neurevolve consists of four windows:
 * <ul>
 * <li>The {@link neurevolve.ui.NewWorldDialog} allows the user to select values for the creation of
 * the world.</li>
 * <li>The {@link neurevolve.ui.MainWindow} displays a map of the world as the organisms develop and
 * allows the user to change the configuration of the world.</li>
 * <li>The {@link neurevolve.ui.ZoomWindow} allows the user to view an expanded view of a section of
 * the world.</li>
 * <li>The {@link neurevolve.ui.AnalysisWindow} performs a statistical sampling of the population to
 * help understand the behaviour of individual organisms.</li>
 * </ul>
 *
 *
 */
public class Neurevolve {

    public static void main(String[] args) {
        new NewWorldDialog().setVisible(true);
    }
}
