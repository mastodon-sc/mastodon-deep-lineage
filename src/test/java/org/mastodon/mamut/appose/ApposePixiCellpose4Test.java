/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2026 Stefan Hahmann
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
package org.mastodon.mamut.appose;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

// @Disabled("Takes several minutes to build the pixi environment. Thus it is disabled.")
class ApposePixiCellpose4Test
{
	static final String ENV_CONTENT = "[workspace]\n"
			+ "name = 'cellpose4-test'\n"
			+ "version = '0.0.1'\n"
			+ "description = 'Mastodon spot detector using Cellpose 4.'\n"
			+ "authors = ['Stefan Hahmann']\n"
			+ "channels = ['conda-forge']\n"
			+ "platforms = ['osx-arm64', 'osx-64', 'linux-64', 'win-64']\n"
			+ "\n"
			+ "[system-requirements]\n"
			+ "cuda = '12'\n"
			+ "\n"
			+ "[dependencies]\n"
			+ "python = '==3.10'\n"
			+ "numpy = '*'\n"
			+ "pip = '*'\n"
			+ "cellpose = '==4.0.6'\n"
			+ "\n"
			+ "[pypi-dependencies]\n"
			+ "appose = '==0.7.2'\n"
			+ "\n"
			+ "[target.win-64.dependencies]\n"
			+ "pytorch-gpu = '*'\n"
			+ "\n"
			+ "[target.linux-64.dependencies]\n"
			+ "pytorch-gpu = '*'\n"
			+ "\n"
			+ "[target.osx-64.dependencies]\n"
			+ "pytorch = '*'\n"
			+ "\n"
			+ "[target.osx-arm64.dependencies]\n"
			+ "pytorch = '*'\n"
			+ "\n"
			+ "[environments]\n"
			+ "default = { solve-group = 'default' }";

	// Mirrors scripts/check_cellpose4.py: imports torch+cellpose, picks an
	// accelerator, segments 9 synthetic blobs, asserts at least one mask.
	private static final String SMOKE_SCRIPT =
			"import numpy as np\n"
					+ "import torch\n"
					+ "import cellpose\n"
					+ "from cellpose import models\n"
					+ "\n"
					+ "print('torch:', torch.__version__)\n"
					+ "print('cellpose:', cellpose.version)\n"
					+ "\n"
					+ "use_cuda = torch.cuda.is_available()\n"
					+ "mps = getattr(torch.backends, 'mps', None)\n"
					+ "mps_available = bool(mps and mps.is_available())\n"
					+ "device = 'cuda' if use_cuda else ('mps' if mps_available else 'cpu')\n"
					+ "print('accelerator:', device)\n"
					+ "\n"
					+ "size = 256\n"
					+ "rng = np.random.default_rng(42)\n"
					+ "img = rng.normal(loc=100, scale=5, size=(size, size)).astype(np.float32)\n"
					+ "step = size // 4\n"
					+ "yy, xx = np.mgrid[0:size, 0:size]\n"
					+ "radius = 15\n"
					+ "for i in range(1, 4):\n"
					+ "    for j in range(1, 4):\n"
					+ "        cy, cx = i * step, j * step\n"
					+ "        mask = (yy - cy) ** 2 + (xx - cx) ** 2 <= radius ** 2\n"
					+ "        img[mask] += 200\n"
					+ "\n"
					+ "model = models.CellposeModel(gpu=use_cuda)\n"
					+ "masks, flows, styles = model.eval(\n"
					+ "    img,\n"
					+ "    diameter=30,\n"
					+ "    do_3D=False,\n"
					+ "    z_axis=None,\n"
					+ "    normalize=True,\n"
					+ "    batch_size=8,\n"
					+ "    flow_threshold=0.4,\n"
					+ "    cellprob_threshold=0.0,\n"
					+ ")\n"
					+ "n_masks = int(masks.max())\n"
					+ "print('masks found:', n_masks)\n"
					+ "assert n_masks >= 1, f'expected at least 1 mask, got {n_masks}'\n"
					+ "print('OK: cellpose segmentation works')\n";

	@Test
	void testApposePixiCellpose4Content() throws InterruptedException, IOException
	{
		Environment env = Appose.pixi().content( ENV_CONTENT ).logDebug().build();
		try (Service python = env.python())
		{
			python.debug( System.out::println );
			Service.Task task = python.task( SMOKE_SCRIPT );
			task.waitFor();
		}
		assertTrue( true, "Appose Pixi cellpose4 environment built and torch/cellpose imported successfully." );
	}

	@Test
	void testApposePixiCellpose4Toml() throws InterruptedException, IOException
	{
		Environment env = Appose.pixi( "src/test/resources/org/mastodon/mamut/appose/cellpose4.toml" ).logDebug().build();
		try (Service python = env.python())
		{
			python.debug( System.out::println );
			Service.Task task = python.task( SMOKE_SCRIPT );
			task.waitFor();
		}
		assertTrue( true, "Appose Pixi cellpose4 environment built and torch/cellpose imported successfully." );
	}
}