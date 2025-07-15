package org.mastodon.mamut.util;

@FunctionalInterface
public interface ToDoubleQuadFunction< A, B, C, D >
{

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param a the first function argument
	 * @param b the second function argument
	 * @param c the third function argument
	 * @param d the fourth function argument
	 * @return the function result as a double
	 */
	double applyAsDouble( A a, B b, C c, D d );
}
