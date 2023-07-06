package org.mastodon.mamut.clustering.ui;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

import java.util.Collection;
import java.util.Map;

public class TagSetUtils
{
	private TagSetUtils()
	{
		// prevent instantiation of utility class
	}

	/**
	 * Add a new tag set to the given model.
	 * @param model The model that will contain the new tag set.
	 * @param name The name of the new tag set.
	 * @param tagsAndColors The list of labels and colors for the new tags. This
	 *            could be a {@link Map#entrySet()} or a list of
	 *            {@link org.apache.commons.lang3.tuple.Pair}s.
	 * @return The new tag set.
	 */
	public static TagSetStructure.TagSet addNewTagSetToModel( Model model, String name,
			Collection< ? extends Map.Entry< String, Integer > > tagsAndColors )
	{
		TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		TagSetStructure original = tagSetModel.getTagSetStructure();
		TagSetStructure newTss = new TagSetStructure();
		newTss.set( original );
		TagSetStructure.TagSet newTagSet = newTss.createTagSet( name );
		for ( Map.Entry< String, Integer > tag : tagsAndColors )
			newTagSet.createTag( tag.getKey(), tag.getValue() );
		tagSetModel.setTagSetStructure( newTss );
		return newTagSet;
	}

	/**
	 * Assigns the specified {@code tag} to the given {@code spot} and all of its outgoing edges.
	 * @param model Model that contains the graph and the tag data structures.
	 * @param spot The spot, that specifies the branch to be tagged.
	 *             Doesn't need to be the branch start or branch end.
	 *             It can be any spot on the branch.
	 * @param tagSet The {@link TagSetStructure.TagSet} the tag belongs to.
	 * @param tag The tag to assign to the branch.
	 */
	public static void tagSpotAndLinks( Model model, Spot spot, TagSetStructure.TagSet tagSet, TagSetStructure.Tag tag )
	{
		ObjTagMap< Spot, TagSetStructure.Tag > vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
		ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
		vertexTags.set( spot, tag );
		for ( Link link : spot.outgoingEdges() )
			edgeTags.set( link, tag );
	}
}
