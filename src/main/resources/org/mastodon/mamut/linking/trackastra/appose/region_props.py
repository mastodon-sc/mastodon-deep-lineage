image_ndarray = {IMAGE}.ndarray()
mask_ndarray = {MASK}.ndarray().astype("int32")

ndim = image_ndarray.ndim
if ndim == 3:
    image = np.expand_dims(np.transpose(image_ndarray, (2, 1, 0)), axis=0)
    mask = np.expand_dims(np.transpose(mask_ndarray, (2, 1, 0)), axis=0)
else:
    image = np.expand_dims(np.transpose(image_ndarray, (1, 0)), axis=0)
    mask = np.expand_dims(np.transpose(mask_ndarray, (1, 0)), axis=0)

task.update(message="Image and mask loaded into numpy arrays")

image = utils.normalize(image)

name = "{MODEL}"
device = "cpu"
download_dir = Path.home() / ".local" / "share" / "appose" / "trackastra" / "pretrained_models"
folder = pretrained.download_pretrained(name=name, download_dir=download_dir)
model = Trackastra.from_folder(dir=folder, device=device)
model.transformer.eval()

features = wrfeat.get_features(
    mask, image, "wrfeat", model.transformer.config["coord_dim"], 0, tqdm
)

labels = features[0].labels
shared_labels = appose.NDArray(str(labels.dtype), labels.shape)
shared_labels.ndarray()[:] = labels

timepoints = features[0].timepoints
shared_timepoints = appose.NDArray(str(timepoints.dtype), timepoints.shape)
shared_timepoints.ndarray()[:] = timepoints

coords = features[0].coords.T
shared_coords = appose.NDArray(str(coords.dtype), coords.shape)
shared_coords.ndarray()[:] = coords

diameter = features[0].features["equivalent_diameter_area"][:, 0]
shared_diameter = appose.NDArray(str(diameter.dtype), diameter.shape)
shared_diameter.ndarray()[:] = diameter

intensity = features[0].features["intensity_mean"][:, 0]
shared_intensity = appose.NDArray(str(intensity.dtype), intensity.shape)
shared_intensity.ndarray()[:] = intensity

inertia_tensor = features[0].features["inertia_tensor"].T
shared_inertia_tensor = appose.NDArray(str(inertia_tensor.dtype), inertia_tensor.shape)
shared_inertia_tensor.ndarray()[:] = inertia_tensor

border_dist = features[0].features["border_dist"][:, 0]
shared_border_dist = appose.NDArray(str(border_dist.dtype), border_dist.shape)
shared_border_dist.ndarray()[:] = border_dist

task.outputs["{LABELS}"] = shared_labels
task.outputs["{TIMEPOINTS}"] = shared_timepoints
task.outputs["{COORDS}"] = shared_coords
task.outputs["{DIAMETER}"] = shared_diameter
task.outputs["{INTENSITY}"] = shared_intensity
task.outputs["{INERTIA_TENSOR}"] = shared_inertia_tensor
task.outputs["{BORDER_DIST}"] = shared_border_dist

task.update(message="Feature extraction completed. Found {} objects".format(len(labels)))
