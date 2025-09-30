labels_ndarray = {LABELS}.ndarray()
timepoints_ndarray = {TIMEPOINTS}.ndarray()
coords_ndarray = {COORDS}.ndarray()
diameters_ndarray = {DIAMETER}.ndarray()
intensities_ndarray = {INTENSITY}.ndarray()
tensors_ndarray = {INERTIA_TENSOR}.ndarray()
border_dists_ndarray = {BORDER_DIST}.ndarray()

wrfeatures_list = []
timepoints = labels_ndarray.shape[1]
for t in range(0, timepoints):
    labels_t = labels_ndarray[:, t]
    num_labels_t = np.count_nonzero(labels_t)
    labels_t = labels_t[:num_labels_t]
    labels_flat = np.asarray(labels_t).ravel()
    sort_idx = np.argsort(labels_t)
    labels_t = labels_t[sort_idx]
    coords_t = coords_ndarray[:, :num_labels_t, t].T
    coords_t = coords_t[sort_idx]
    coords_t = coords_t[:, ::-1]  # reverse order
    timepoints_t = timepoints_ndarray[:num_labels_t, t]
    timepoints_t = timepoints_t[sort_idx]
    diameters_t = diameters_ndarray[:num_labels_t, [t]]
    diameters_t = diameters_t[sort_idx]
    intensities_t = intensities_ndarray[:num_labels_t, [t]]
    intensities_t = intensities_t[sort_idx]
    tensors_t = tensors_ndarray[:, :num_labels_t, t].T
    tensors_t = tensors_t[sort_idx]
    tensors_t = tensors_t[:, ::-1]  # reverse order
    border_dists_t = border_dists_ndarray[:num_labels_t, [t]]
    border_dists_t = border_dists_t[sort_idx]
    features_t = OrderedDict()
    features_t['equivalent_diameter_area'] = diameters_t
    features_t['intensity_mean'] = intensities_t
    features_t['inertia_tensor'] = tensors_t
    features_t['border_dist'] = border_dists_t
    wrfeatures_t = wrfeat.WRFeatures(coords=coords_t, labels=labels_t, timepoints=timepoints_t, features=features_t)
    wrfeatures_list.append(wrfeatures_t)

task.update(message='Read data from Region Props')

features = tuple(wrfeatures_list)
name = '{MODEL}'
device = 'cpu'
download_dir = Path.home() / '.local' / 'share' / 'appose' / 'trackastra' / 'pretrained_models'
folder = pretrained.download_pretrained(name=name, download_dir=download_dir)
model = Trackastra.from_folder(dir=folder, device=device)

task.update(message=f'(Downloaded) and loaded pretrained model. Folder: {folder}')

window_size = {WINDOW_SIZE}
windows = wrfeat.build_windows(features, window_size, tqdm, True)

task.update(message='Window building from features completed')

model.transformer.eval()
predictions = predict.predict_windows(windows, features, model.transformer, 0, 1, {EDGE_THRESHOLD}, {NUM_DIMENSIONS}, 1,
                                      tqdm)
task.update(message='Predictions completed')

track_graph = model._track_from_predictions(predictions, '{MODE}')
nodes = track_graph.number_of_nodes()
n_edges = track_graph.number_of_edges()

task.update(message='Tracking graph construction completed. Nodes: ' + str(nodes) + ', Edges: ' + str(n_edges) + '')

edges_table = utils.graph_to_edge_table(track_graph)
edges = edges_table.to_numpy(dtype=np.float32)
shared_edges = appose.NDArray(str(edges.dtype), edges.shape)
shared_edges.ndarray()[:] = edges
task.outputs['{EDGES}'] = shared_edges

task.update(message=str(n_edges) + ' edges saved to shared memory')
