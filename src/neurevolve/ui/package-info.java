/**
 * The user interface for Neurevolve. The application has the following windows:
 *
 * <h3>New Window Dialog</h3>
 *
 * This simple dialog is used to config a new window. The user can set values such as the size,
 * terrain, seeding population and initial resources before creating the world.
 *
 * <h3>Main Window</h3>
 *
 * This is the main display for the application. It shows a view of the entire world with one pixel
 * per position in the space displaying the organisms, resources and elevatioin. The speed of
 * updates can be slowed.
 *
 * The window also provides access to configuration items that can be changed dynamically.
 *
 * <h3>Zoom Window</h3>
 *
 * The zoom window provides an expanded view of a small section of the world. It takes a series of
 * snapshots of the population to allow the behaviour of individual organisms to be examined. The
 * network driving the behaviour of a single organism can be displayed.
 *
 * <h3>Analysis Window</h3>
 *
 * The analysis window is used to analyse the population in the main window. It divides the
 * population into species and displays the list of specie. Individual species can then have their
 * recipes viewed.
 */
package neurevolve.ui;
