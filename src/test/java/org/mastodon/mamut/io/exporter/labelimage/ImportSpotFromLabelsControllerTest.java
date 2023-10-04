package org.mastodon.mamut.segment;

import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.scijava.Context;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ImportSpotFromLabelsControllerTest
{
	private Model model;

	private int timepoint;

	@Before
	public void setUp()
	{
		model = new Model();
		ModelBranchGraph modelBranchGraph = model.getBranchGraph();
		modelBranchGraph.graphRebuilt();
		timepoint = 0;
	}

	@Test
	public void testGetEllipsoidFromImage() {
		AbstractSource<IntType> img = createImage();

		Context context = new Context(true);
		TimePoint timePoint = new TimePoint( timepoint );
		List< TimePoint > timePoints = Collections.singletonList( timePoint );
		VoxelDimensions voxelDimensions = new FinalVoxelDimensions("um", 0.16, 0.16, 1);
		ImportSpotFromLabelsController controller = new ImportSpotFromLabelsController(model, timePoints, img, context, voxelDimensions, 2.2);

		controller.createSpotsFromLabels();

		Iterator<Spot> iter = model.getGraph().vertices().iterator();
		assertTrue(iter.hasNext());

		Spot s = iter.next();

		s.getDoublePosition(0);
	}

	private static AbstractSource< IntType > createImage()
	{
		Img<IntType> img = new ArrayImgFactory<>(new IntType()).create(4, 4, 4);
		RandomAccess<IntType> ra = img.randomAccess();
		ra.setPositionAndGet(1, 1, 1).set(1);
		ra.setPositionAndGet(2, 2, 2).set(1);
		ra.setPositionAndGet(3, 3, 3).set(1);

		return new RandomAccessibleIntervalSource<>( img, new IntType(), new AffineTransform3D(), "Segmentation" );
	}
}
