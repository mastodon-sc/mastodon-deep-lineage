package org.mastodon.mamut.detection;

public class StarDist3D extends Segmentation3D
{
	private final String starDistModelPath;

	public StarDist3D( final String starDistModelPath )
	{
		this.starDistModelPath = starDistModelPath;
	}

	@Override
	String generateEnvFileContent()
	{
		return "name: stardist\n"
				+ "channels:\n"
				+ "  - conda-forge\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ "  - cudatoolkit=11.2\n"
				+ "  - cudnn=8.1.0\n"
				+ "  - numpy<1.24\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - numpy<1.24\n"
				+ "    - tensorflow==2.10\n"
				+ "    - stardist==0.8.5\n"
				+ "    - appose\n";
	}

	@Override
	String generateScript()
	{
		return "import numpy as np" + "\n"
				+ "import appose" + "\n"
				+ "from csbdeep.utils import normalize" + "\n"
				+ "from stardist.models import StarDist3D" + "\n\n"
				+ "np.random.seed(6)" + "\n"
				+ "axes_normalize = (0, 1, 2)" + "\n\n"
				+ "print(\"Loading StarDist pretrained 3D model\")" + "\n"
				// + "model = StarDist3D.from_pretrained('3D_demo')" + "\n"
				+ "model = StarDist3D(None, name='stardist-plant-nuclei-3d', basedir=r\"" + starDistModelPath + "\")"
				+ "\n"
				+ "image_ndarray = image.ndarray()" + "\n"
				+ "image_normalized = normalize(image_ndarray, 1, 99.8, axis=axes_normalize)" + "\n"
				+ "print(\"Image shape:\", image_normalized.shape)" + "\n\n"
				+ "guessed_tiles = model._guess_n_tiles(image_normalized)" + "\n"
				+ "print(\"Guessed tiling:\", guessed_tiles)" + "\n\n"
				+ "label_image, details = model.predict_instances(image_normalized, axes='ZYX', n_tiles=guessed_tiles)" + "\n"
				+ "shared = appose.NDArray(image.dtype, image.shape)" + "\n"
				+ "shared.ndarray()[:] = label_image" + "\n"
				+ "task.outputs['label_image'] = shared" + "\n";
	}
}
