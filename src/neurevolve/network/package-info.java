/**
 * A neural network is a mechanism to control a set of activities from a given set of inputs. The
 * network consists of a series of neurons that read values from inputs from the environment
 * and calculate an output value. Depending on the value, the neurons may trigger an activity
 * in the environment. The output of one neuron can operate as the input to another allowing
 * complex emergent behaviour for the overall network.
 *
 * This package contains the classes and interfaces necessary to create and operate a simple
 * neural network. The network consists of a single list of neurons. It is designed to be independent of
 * the environment the network operates within. All inputs and outputs from the network conform to
 * the <code>Input</code> and <code>Activity</code> interfaces. A network supports links from
 * earlier neurons to later neurons.
 *
 * Neurons use an {@link ActivationFunction} to determine the relationship between input and output
 * values. A standard {@link SigmoidFunction} is provided but substituted activation functions can
 * be used at construction of the network.
 */
package neurevolve.network;
