package org.mastodon.mamut.util;

@FunctionalInterface
public interface ToDoubleTriFunction< A, B, C >
{

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param a the first function argument
	 * @param b the second function argument
	 * @param c the third function argument
	 * @return the function result as a double
	 */
	double applyAsDouble( A a, B b, C c );
}
