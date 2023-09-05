package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;

/**
 * WPGMA (Weighted Pair Group Method with Arithmetic Mean) is a simple agglomerative (bottom-up) hierarchical clustering method
 * 
 * @see <a href="https://en.wikipedia.org/wiki/WPGMA">WPGMA</a>
 * @see <a href="https://www.youtube.com/watch?v=T1ObCUpjq3o">Example how to compute hierarchical clustering with average linkage</a>. NB: the video falsely states that UPGMA is used, while it is actually explaining WPGMA.
 */
public class AverageLinkageWPGMAStrategy extends AverageLinkageStrategy
{}
