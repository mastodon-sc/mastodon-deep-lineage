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

axes_normalize = (0, 1, 2)

task.update(message='Loading StarDist model')

{MODEL}

image_ndarray = image.ndarray()
image_normalized = normalize(image_ndarray, 1, 99.8, axis=axes_normalize)

task.update(message='Image shape: ' + str(image_normalized.shape))
guessed_tiles = model._guess_n_tiles(image_normalized)

task.update(message='Guessed tiles: ' + str(guessed_tiles))

label_image, details = model.predict_instances(image_normalized, axes='{AXES}', n_tiles=guessed_tiles,
                                               nms_thresh={NMS_THRESH}, prob_thresh={PROB_THRESH})

shared = appose.NDArray(image.dtype, image.shape)
shared.ndarray()[:] = label_image
task.outputs['label_image'] = shared
