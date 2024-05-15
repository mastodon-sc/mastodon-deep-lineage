package org.mastodon.mamut.clustering;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImgToVirtualStack;
import net.imglib2.type.numeric.integer.ByteType;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import java.util.Objects;

public class DummyProjectModel
{
	public static ProjectModel createModel( final Model model )
	{
		try (Context context = new Context())
		{
			final Img< ByteType > dummyImg = ArrayImgs.bytes( 1, 1, 1 );
			final ImagePlus dummyImagePlus =
					ImgToVirtualStack.wrap( new ImgPlus<>( dummyImg, "image", new AxisType[] { Axes.X, Axes.Y, Axes.Z } ) );
			SharedBigDataViewerData dummyBdv = Objects.requireNonNull( SharedBigDataViewerData.fromImagePlus( dummyImagePlus ) );
			return ProjectModel.create( context, model, dummyBdv, null );
		}
	}

	public static ProjectModel createModel()
	{
		return createModel( new Model() );
	}
}
