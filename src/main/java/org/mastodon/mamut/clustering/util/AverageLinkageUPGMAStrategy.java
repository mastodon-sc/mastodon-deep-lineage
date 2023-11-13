package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.WeightedLinkageStrategy;

/**
 * UPGMA (unweighted pair group method with arithmetic mean) is a simple agglomerative (bottom-up) hierarchical clustering method.
 *
 * @see <a href="https://en.wikipedia.org/wiki/UPGMA">UPGMA</a>
 *
 * This class extends the class {@link WeightedLinkageStrategy} from the hierarchical clustering library, since the naming of that class was misleading.
 */
public class AverageLinkageUPGMAStrategy extends WeightedLinkageStrategy
{}
