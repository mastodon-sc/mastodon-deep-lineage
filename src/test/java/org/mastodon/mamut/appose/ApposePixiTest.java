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
package org.mastodon.mamut.appose;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apposed.appose.Appose;
import org.apposed.appose.Builder;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;
import org.junit.jupiter.api.Test;

class ApposePixiTest
{
	static final String ENV_CONTENT = "[workspace]\n"
			+ "name = 'stardist-test'\n"
			+ "version = '0.0.1'\n"
			+ "description = 'Mastodon spot detector using StarDist3D.'\n"
			+ "authors = ['Curtis Rueden', 'Carlos Garcia', 'Jean-Yves Tinevez', 'Stefan Hahmann']\n"
			+ "channels = ['conda-forge']\n"
			+ "platforms = ['osx-arm64', 'osx-64', 'linux-64', 'win-64']\n"
			+ "\n"
			+ "[dependencies]\n"
			+ "python = '==3.10'\n"
			+ "numpy = '<2'\n"
			+ "scipy = '*'\n"
			+ "pip = '*'\n"
			+ "\n"
			+ "[pypi-dependencies]\n"
			+ "appose = '==0.7.2'\n"
			+ "stardist = '==0.9.1'\n"
			+ "csbdeep = '*'\n"
			+ "ndv = { extras = ['qt'] }\n"
			+ "pygfx = '<0.13.0'\n"
			+ "\n"
			+ "[target.osx-64.dependencies]\n"
			+ "python = '*'\n"
			+ "\n"
			+ "[target.osx-arm64.dependencies]\n"
			+ "python = '*'\n"
			+ "\n"
			+ "# PyPI deps common per-OS:\n"
			+ "[target.win-64.pypi-dependencies]\n"
			+ "tensorflow = '==2.10.1'\n"
			+ "\n"
			+ "[target.linux-64.pypi-dependencies]\n"
			+ "tensorflow = '==2.10.1'\n"
			+ "\n"
			+ "[target.osx-64.pypi-dependencies]\n"
			+ "tensorflow = '*'                 # latest CPU TF for Intel mac\n"
			+ "\n"
			+ "[target.osx-arm64.pypi-dependencies]\n"
			+ "tensorflow-macos = '*'           # latest TF for Apple Silicon\n"
			+ "tensorflow-metal = '*'           # Metal plugin\n"
			+ "\n"
			+ "# ---------------- Macos no metal - ONLY APPLE SILICON ----------------\n"
			+ "[feature.nometal]\n"
			+ "channels = ['conda-forge']\n"
			+ "\n"
			+ "[feature.nometal.target.osx-arm64.pypi-dependencies]\n"
			+ "tensorflow = '*'\n"
			+ "\n"
			+ "# ---------------- CUDA feature (Windows/Linux only) ----------------\n"
			+ "[feature.cuda]\n"
			+ "channels = ['nvidia', 'conda-forge']   # optional; inherits from workspace\n"
			+ "\n"
			+ "[feature.cuda.target.win-64.dependencies]\n"
			+ "cudatoolkit = '11.2.*'\n"
			+ "cudnn = '8.1.*'\n"
			+ "\n"
			+ "[feature.cuda.target.linux-64.dependencies]\n"
			+ "cudatoolkit = '11.2.*'\n"
			+ "cudnn = '8.1.*'\n"
			+ "\n"
			+ "# Activation only when the 'cuda' feature is part of the active environment:\n"
			+ "[feature.cuda.target.win-64.activation.env]\n"
			+ "PATH = '%CONDA_PREFIX%\\\\\\\\Library\\\\\\\\bin;%PATH%'\n"
			+ "\n"
			+ "[feature.cuda.target.linux-64.activation.env]\n"
			+ "LD_LIBRARY_PATH = '$CONDA_PREFIX/lib:${LD_LIBRARY_PATH:-}'\n"
			+ "\n"
			+ "# Dev feature\n"
			+ "[feature.dev.dependencies]\n"
			+ "pytest = '*'\n"
			+ "ruff = '*'\n"
			+ "\n"
			+ "[feature.dev.pypi-dependencies]\n"
			+ "build = '*'\n"
			+ "validate-pyproject = { extras = ['all'] }\n"
			+ "\n"
			+ "# ---------------- TASKS (cmd holds the whole command line) ---------------- #\n"
			+ "[tasks]\n"
			+ "# Main application\n"
			+ "start = 'python src/main.py'\n"
			+ "\n"
			+ "# Development tasks\n"
			+ "lint = 'ruff check --fix && ruff format'\n"
			+ "test = 'pytest -v -p no:faulthandler tests'\n"
			+ "validate = 'validate-pyproject pyproject.toml'\n"
			+ "dist = 'python -m build'\n"
			+ "\n"
			+ "# Combined tasks\n"
			+ "check = { depends-on = ['validate', 'lint'] }\n"
			+ "\n"
			+ "# ---------------- ENVIRONMENTS ---------------- #\n"
			+ "\n"
			+ "[environments]\n"
			+ "default = { solve-group = 'default' }\n"
			+ "cuda = { features = ['cuda'], solve-group = 'default' }\n"
			+ "nometal = { features = ['nometal'], solve-group = 'default' }\n"
			+ "dev = { features = ['dev'], solve-group = 'default' }\n"
			+ "cuda-dev = { features = ['cuda', 'dev'], solve-group = 'default' }";

	@Test
	void testApposePixi() throws InterruptedException, IOException
	{
		Environment env = Appose.pixi().content( ENV_CONTENT ).logDebug().build();
		List< String > launchArgs = new ArrayList<>( env.launchArgs() );
		launchArgs.add( 2, "-e" );
		launchArgs.add( 3, "cuda" );
		Environment cudaEnv = new Environment()
		{
			@Override
			public String base()
			{
				return env.base();
			}

			@Override
			public List< String > binPaths()
			{
				return env.binPaths();
			}

			@Override
			public List< String > launchArgs()
			{
				return launchArgs;
			}

			@Override
			public Map< String, String > envVars()
			{
				return env.envVars();
			}

			@Override
			public Builder< ? > builder()
			{
				return env.builder();
			}
		};
		try (Service python = cudaEnv.python())
		{
			python.debug( System.out::println );
			Service.Task task = python.task( "import tensorflow as tf\n"
					+ "print(tf.config.list_physical_devices('GPU'))\n" );
			task.waitFor();
		}
		assertTrue( true, "Appose Pixi environment built and gpu devices printed successfully." );
	}

	@Test
	void testApposePixiToml() throws InterruptedException, IOException
	{
		Environment env = Appose.pixi( "src/test/resources/org/mastodon/mamut/appose/stardist.toml" ).logDebug().build();
		List< String > launchArgs = new ArrayList<>( env.launchArgs() );
		launchArgs.add( 2, "-e" );
		launchArgs.add( 3, "cuda" );
		Environment cudaEnv = new Environment()
		{
			@Override
			public String base()
			{
				return env.base();
			}

			@Override
			public List< String > binPaths()
			{
				return env.binPaths();
			}

			@Override
			public List< String > launchArgs()
			{
				return launchArgs;
			}

			@Override
			public Map< String, String > envVars()
			{
				return env.envVars();
			}

			@Override
			public Builder< ? > builder()
			{
				return env.builder();
			}
		};
		try (Service python = cudaEnv.python())
		{
			python.debug( System.out::println );
			Service.Task task = python.task( "import tensorflow as tf\n"
					+ "print(tf.config.list_physical_devices('GPU'))\n" );
			task.waitFor();
		}
		assertTrue( true, "Appose Pixi environment built and gpu devices printed successfully." );
	}

	@Test
	void testApposePixiTomlGpu() throws InterruptedException, IOException
	{
		Environment env = Appose.pixi( "src/test/resources/org/mastodon/mamut/appose/stardist-gpu.toml" ).logDebug().build();
		try (Service python = env.python())
		{
			python.debug( System.out::println );
			Service.Task task = python.task( "import tensorflow as tf\n"
					+ "print(tf.config.list_physical_devices('GPU'))\n" );
			task.waitFor();
		}
		assertTrue( true, "Appose Pixi environment built and gpu devices printed successfully." );
	}
}
