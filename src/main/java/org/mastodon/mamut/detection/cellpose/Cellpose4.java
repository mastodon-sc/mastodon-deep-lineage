/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.detection.cellpose;

import java.io.IOException;

/**
 * Cellpose3 is a specialized implementation of the {@link Cellpose} class, specifically
 * designed to use Cellpose version 4 model for cell segmentation tasks.<br>
 */
public class Cellpose4 extends Cellpose
{
	public Cellpose4() throws IOException
	{
		super();
	}

	@Override
	protected String getLoadModelCommand()
	{
		return "model = models.CellposeModel(gpu=True)" + "\n";
	}

	@Override
	protected String getEvaluateModelCommand()
	{
		return "segmentation, flows, styles = model.eval("
				+ "image_ndarray, "
				+ "diameter=" + getDiameter() + ", "
				+ "do_3D=" + is3DParam() + ", "
				+ "z_axis=0, "
				+ "normalize=True, "
				+ "batch_size=8, "
				+ "flow3D_smooth=0, "
				+ "flow_threshold=" + flowThreshold + ", "
				+ "cellprob_threshold=" + cellProbThreshold + ")" + "\n";
	}

	@Override
	protected String generateEnvFileContent()
	{
		return "name: cellpose4\n"
				+ "channels:\n"
				+ "  - nvidia\n"
				+ "  - pytorch\n"
				+ "  - conda-forge\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - cellpose==4.0.2\n"
				// + "    - git+https://github.com/apposed/appose-python.git@efe6dadb2242ca45820fcbb7aeea2096f99f9cb2\n" // contains a bug fix to run appose on Windows
				+ "    - appose==0.4.0\n"
				+ "  - pytorch\n"
				+ "  - pytorch-cuda\n"
				+ "  - numpy\n";
	}
}
