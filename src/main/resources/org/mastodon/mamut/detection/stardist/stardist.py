###
# #%L
# mastodon-deep-lineage
# %%
# Copyright (C) 2022 - 2025 Stefan Hahmann
# %%
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
# #L%
###
# Configure TensorFlow to use GPU
task.update(f'TensorFlow version: {tf.__version__}')
gpu_devices = tf.config.list_physical_devices('GPU')
if gpu_devices:
    task.update(f'GPU devices available: {len(gpu_devices)}')
    for device in gpu_devices:
        task.update(f'  - {device.name}: {device.device_type}')

    # Enable GPU memory growth to avoid OOM
    try:
        for gpu in gpu_devices:
            tf.config.experimental.set_memory_growth(gpu, True)
        task.update('Enabled GPU memory growth')
    except Exception as e:
        task.update(f'Could not set memory growth: {e}')

    # Set visible devices to ensure GPU is used
    tf.config.set_visible_devices(gpu_devices, 'GPU')
    task.update('Set GPU as visible device')
else:
    task.update('No GPU devices found - running on CPU')

np.random.seed(6)

axes_normalize = {AXES_NORMALIZE}

estimated_diameter_xy = {ESTIMATED_DIAMETER_XY}
estimated_diameter_z = {ESTIMATED_DIAMETER_Z}
expected_diameter_xy = {EXPECTED_DIAMETER_XY}
expected_diameter_z = {EXPECTED_DIAMETER_Z}

if expected_diameter_xy >= 0 and estimated_diameter_xy >= 0:
    scale_xy = estimated_diameter_xy / expected_diameter_xy
else:
    scale_xy = 1.0
if expected_diameter_z >= 0 and estimated_diameter_z >= 0:
    scale_z = estimated_diameter_z / expected_diameter_z
else:
    scale_z = 1.0

task.update(f"Model expects nucleus size: XY={EXPECTED_DIAMETER_XY}px, Z={EXPECTED_DIAMETER_Z}px")
task.update(f"User estimates nucleus size: XY={ESTIMATED_DIAMETER_XY}px, Z={ESTIMATED_DIAMETER_Z}px")
task.update(f"Scaling factors: XY={scale_xy:.3f}, Z={scale_z:.3f}")

task.update(message='Loading StarDist model')

{MODEL}

frame = image.ndarray()

zoom_factors = [1.0] * frame.ndim
if frame.ndim == 3:
    zoom_factors[0] = scale_z
    zoom_factors[1] = scale_xy
    zoom_factors[2] = scale_xy
elif frame.ndim == 2:
    zoom_factors[0] = scale_xy
    zoom_factors[1] = scale_xy

task.update(f"Scaling volume with factors: {zoom_factors}")
frame_scaled = zoom(frame, zoom_factors, order=1)  # Linear interpolation
task.update(f"Scaled shape: {frame.shape} -> {frame_scaled.shape}")

task.update(f"Dimensions: {frame.ndim}")
inverse_zoom_factors = [1.0] * frame.ndim
if frame.ndim == 3:
    inverse_zoom_factors[0] = frame.shape[0] / frame_scaled.shape[0]
    inverse_zoom_factors[1] = frame.shape[1] / frame_scaled.shape[1]
    inverse_zoom_factors[2] = frame.shape[2] / frame_scaled.shape[2]
elif frame.ndim == 2:
    inverse_zoom_factors[0] = frame.shape[0] / frame_scaled.shape[0]
    inverse_zoom_factors[1] = frame.shape[1] / frame_scaled.shape[1]

frame_scaled = normalize(frame_scaled, 1, 99.8, axis=axes_normalize)
task.update(message='Image shape for prediction: ' + str(frame_scaled.shape))

# Ensure float32 for TensorFlow
if frame_scaled.dtype != np.float32:
    task.update(f"Converting to float32 from {frame_scaled.dtype}")
    frame_scaled = frame_scaled.astype(np.float32)

guessed_tiles = model._guess_n_tiles(frame_scaled)
task.update(message='Guessed tiles: ' + str(guessed_tiles))
task.update("Starting prediction")

label_image, details = model.predict_instances(frame_scaled, axes='{AXES}', n_tiles=guessed_tiles,
                                               nms_thresh={NMS_THRESH}, prob_thresh={PROB_THRESH})

task.update("Prediction finished")

# Scale labels back to original size
task.update(f"Unscaling volume with factors: {inverse_zoom_factors}")
label_image = zoom(label_image.astype(np.float32), inverse_zoom_factors, order=0)  # Nearest neighbor
task.update(message='Image size after unscaling: ' + str(label_image.shape))
label_image = label_image.astype(label_image.dtype)
task.update("Done unscaling")


shared = appose.NDArray(image.dtype, image.shape)
shared.ndarray()[:] = label_image
task.outputs['label_image'] = shared
