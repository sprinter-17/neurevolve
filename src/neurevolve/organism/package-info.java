/**
 * An organism represents a single unit of behaviour in an environment. Each organism contains a
 * single neural network that controls the activities it performs. Performing activities causes
 * the organism's energy to increase and decrease. If it reaches zero the organism dies and can no
 * longer act.
 *
 * This package contains the classes and interfaces necessary to create organisms and allow them
 * to act in an environment. It defines a simple interface to allow the organism to interact with
 * its environment.
 *
 * Organisms are designed to be created from a <code>Recipe</code> which contains a set of
 * instructions for building a network. When an organism splits, its recipe is copied to the created
 * organism. The copying process is completed by the organism's environment which allows the
 * inclusion of transcription errors in the copy to drive a process of evolution.
 */
package neurevolve.organism;
