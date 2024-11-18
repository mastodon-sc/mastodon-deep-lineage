package org.mastodon.mamut.util;

/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure;

/**
 * Collection of utilities related to manipulating {@link org.mastodon.model.tag.TagSetStructure.TagSet}
 *
 * @author Matthias Arzt
 * @author Stefan Hahmann
 * @see TagSetStructure
 *
 * // TODO: This class is a partial duplicate of org.mastodon.mamut.util.TagSetUtils. It should be removed after beta-30 release is available on Fiji Mastodon update site.
 */
public class LegacyTagSetUtils
{
	private LegacyTagSetUtils()
	{
		// prevent instantiation of utility class
	}

	/**
	 * If all spots and links of the branch have the same tag, return that tag,
	 * return null otherwise.
	 *
	 * @param model
	 *            The model that contains the graph and the tags.
	 * @param tagSet
	 *            The tag set to consider.
	 * @param branchStart
	 *            The spot at the start of the branch.
	 * @return the tag or null.
	 */
	public static TagSetStructure.Tag getBranchTag( final Model model, final TagSetStructure.TagSet tagSet, final Spot branchStart )
	{
		final ModelGraph graphA = model.getGraph();
		Spot s = graphA.vertexRef();
		try
		{
			final ObjTagMap< Spot, TagSetStructure.Tag > vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
			final ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
			final TagSetStructure.Tag tag = vertexTags.get( branchStart );
			if ( tag == null )
				return null;
			// forward
			s.refTo( branchStart );
			while ( s.outgoingEdges().size() == 1 )
			{
				final Link link = s.outgoingEdges().get( 0 );
				s = link.getTarget( s );
				if ( s.incomingEdges().size() != 1 )
					break;
				if ( !tag.equals( edgeTags.get( link ) ) )
					return null;
				if ( !tag.equals( vertexTags.get( s ) ) )
					return null;
			}
			return tag;
		}
		finally
		{
			graphA.releaseRef( s );
		}
	}

	/**
	 * Gets the tag label of the first spot in the given branchSpot within the given tagSet.
	 * @param model the model to which the branch belongs
	 * @param branchSpot the branch spot
	 * @param tagSet the tag set
	 * @return the tag label
	 */
	public static String getTagLabel( final Model model, final BranchSpot branchSpot, final TagSetStructure.TagSet tagSet, final Spot ref )
	{
		if ( model == null || branchSpot == null || tagSet == null )
			return null;
		Spot first = getFirstSpot( model, branchSpot, ref );
		TagSetStructure.Tag tag = getBranchTag( model, tagSet, first );
		return tag == null ? null : tag.label();
	}

	/**
	 * Gets the first {@link Spot} within the given {@link BranchSpot}.
	 * @param model the {@link Model} to which the {@link BranchSpot} belongs
	 * @param branchSpot the {@link BranchSpot} to query
	 * @return the first {@link Spot}
	 */
	private static Spot getFirstSpot( final Model model, final BranchSpot branchSpot, final Spot ref )
	{
		return model.getBranchGraph().getFirstLinkedVertex( branchSpot, ref );
	}
}
