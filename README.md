[![Build Status](https://github.com/mastodon-sc/mastodon-deep-lineage/actions/workflows/build.yml/badge.svg)](https://github.com/mastodon-sc/mastodon-deep-lineage/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-BSD%202--Clause-orange.svg)](https://opensource.org/licenses/BSD-2-Clause)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mastodon-sc_mastodon-deep-lineage&metric=coverage)](https://sonarcloud.io/summary/overall?id=mastodon-sc_mastodon-deep-lineage)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=mastodon-sc_mastodon-deep-lineage&metric=ncloc)](https://sonarcloud.io/summary/overall?id=mastodon-sc_mastodon-deep-lineage)
[![DOI](https://zenodo.org/badge/574570443.svg)](https://zenodo.org/doi/10.5281/zenodo.10262664)
[![YouTube](https://badges.aleen42.com/src/youtube.svg)](https://www.youtube.com/playlist?list=PL0D04vXRhSYGK9kmAFsIaUx72ujVRWUUs)

# Mastodon Deep Lineage - a collection of plugins to analyse lineages of tracked objects in Mastodon

## Table of Contents

* [Documentation of Mastodon](#documentation-of-mastodon)
* [Installation Instructions](#installation-instructions)
* [Numerical Features added to Mastodon](#numerical-features-added-to-mastodon)
    * [Spot Features](#spot-features)
    * [Branch Features](#branch-features)
* [Hierarchical Clustering of Lineage Trees](#hierarchical-clustering-of-lineage-trees)
    * [Zhang Tree Edit Distance](#zhang-tree-edit-distance)
    * [Workflow](#workflow)
    * [Parameters](#parameters)
    * [Example](#example)
* [Dimensionality reduction](#dimensionality-reduction)
    * [Usage](#usage)
    * [Description](#description)
    * [Parameters](#parameters)
* [Import](#import)
    * [Import Spots from Segmented Label Image](#import-spots-from-segmented-label-image)
* [Export](#export)
    * [Label Image Exporter](#label-image-export)
    * [GraphML Export](#graphml-export)
* [Technical Information](#technical-information)
    * [Maintainer](#maintainer)
    * [Contributors](#contributors)
    * [License](#license)
    * [Contribute Code or Provide Feedback](#contribute-code-or-provide-feedback)
    * [Contribute Documentation](#contribute-documentation)
* [Acknowledgements](#acknowledgements)

## Documentation of Mastodon

Mastodon Deep Lineage is an extension of Mastodon. For the full documentation of Mastodon, please visit
[mastodon.readthedocs.io](https://mastodon.readthedocs.io/en/latest/index.html).

## Installation Instructions

* Add the listed Mastodon update sites in Fiji:
    * `Help > Update > Manage update sites`
        1. `Mastodon`
        2. `Mastodon-DeepLineage`
           ![Mastodon Update sites](doc/installation/Mastodon.png)

## Numerical Features added to Mastodon

### Spot Features

| **Feature name**             | **Projections**                                   | **Description**                                                                                                                                                                           | **Formula/Visualisation**                                                                                              |
|------------------------------|---------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| Spot Ellipsoid               | Short semi axes, Middle semi axis, Long semi axis | The ellipsoid semi axes in ascending order of length.                                                                                                                                     | The semi axes are computed applying the square root to the eigenvalues of the so-called covariance matrix of the spots |
|                              | Volume                                            | The volume of the ellipsoid. <br><br> Example visualization: ![spotVolumeAnimation.gif](doc/features/spot/spotVolumeAnimation.gif)                                                        | ![](doc/features/spot/spotVolume.gif)                                                                                  |
| Spot Ellipsoid Aspect Ratios | Aspect ratio short to middle                      | The ratio between the short axis and middle axis.                                                                                                                                         | ![](doc/features/spot/spotShortMiddleAxis.gif)                                                                         |
|                              | Aspect ratio short to long                        | The ratio between the short axis and long axis. <br><br> Example visualization: ![spotEllispoidAspectRatioAnimation.gif](doc/features/spot/spotEllispoidAspectRatioAnimation.gif)         | ![](doc/features/spot/spotShortLongAxis.gif)                                                                           |
|                              | Aspect ratio middle to long                       | The ratio between the middle axis and long axis.                                                                                                                                          | ![](doc/features/spot/spotMiddleLongAxis.gif)                                                                          |
| Spot Branch ID               | _idem_                                            | The ID of the branch spot each spot belongs to.                                                                                                                                           |                                                                                                                        |
| Spot Relative Movement*      | x, y and z component                              | The x, y and z components of the movement distance of a spot relative to its `n` nearest neighbors. The number of neighbors to be considered can be specified by the users. Default is 5. | ![](doc/features/spot/spotRelativeMovementEquation1.gif)                                                               |
|                              | distance                                          | The movement distance relative to `n` nearest neighbors.                                                                                                                                  | ![](doc/features/spot/spotRelativeMovementEquation2.gif)                                                               |

### Branch Features

| **Feature name**                     | **Projections**      | **Description**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              | **Formula/Visualisation**                                                                                                             |
|--------------------------------------|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| Branch N Leaves                      | _idem_               | The total number of leaves of a branch spot in the whole track subtree of this branch spot. <br><br> Example visualization: ![branchNLeavesAnimation.gif](doc/features/branch/branchNLeavesAnimation.gif)                                                                                                                                                                                                                                                                                                                                                                    | ![](doc/features/branch/branchNLeaves.png)                                                                                            |
| Branch N Successors and Predecessors | _idem_               | Total number of successors and predecessors of a branch spot in the whole track subtree of this branch spot. <br><br> Example visualization: <br> Successors (encodes reproductivity) ![branchNSuccessorsAnimation.gif](doc/features/branch/branchNSuccessorsAnimation.gif) <br> Predecessors (encodes generation) ![branchNPredecessorsAnimation.gif](doc/features/branch/branchNPredecessorsAnimation.gif)                                                                                                                                                                 | ![](doc/features/branch/branchNSuccessorsPredecessors.png)                                                                            |
| Branch Sinuosity                     | _idem_               | The sinuosity of a spot during its life span (cf. [Sinuosity](https://en.wikipedia.org/wiki/Sinuosity)), i.e. how much the track represented by the branch is curved. Values close to 1: almost straight movement. Values significantly higher than 1: winding or meandering movement. Positive infinity (∞), if the spot is at the end at the same position as at the beginning, but has moved in between. <br><br> Example visualization: ![branchSinuosityAnimation.gif](doc/features/branch/branchSinuosityAnimation.gif)                                                | ![](doc/features/branch/branchSinuosityEquation.gif)                                                                                  |
| Branch Average Movement              | _idem_               | The average movement per frame of a spot during its life span. Example visualization: ![branchAverageMovementAnimation.gif](doc/features/branch/branchAverageMovementAnimation.gif)                                                                                                                                                                                                                                                                                                                                                                                          | ![](doc/features/branch/branchAverageMovementEquation.gif) <br><br> e.g.: <br><br> ![](doc/features/branch/branchAverageMovement.png) |
| Branch Movement Direction            | _idem_               | The movement direction of a branch spot represented as a normalized directional vector pointing from the start (spot) position to the end (spot) position of the BranchSpot. <br><br> Example visualizations: <br> x-component ![branchMovementDirectionXAnimation.gif](doc/features/branch/branchMovementDirectionXAnimation.gif) <br> y-component ![branchMovementDirectionYAnimation.gif](doc/features/branch/branchMovementDirectionYAnimation.gif) <br> z-component ![branchMovementDirectionZAnimation.gif](doc/features/branch/branchMovementDirectionZAnimation.gif) | ![](doc/features/branch/branchMovementDirectionEquation.gif)                                                                          |
| Branch Cell Division Frequency       | _idem_               | Number of cell divisions in the subtree rooted at each Branch-spot divided by total duration of branches in this sub-tree. <br><br> Example visualization: ![branchCellDivisionFrequencyAnimation.gif](doc/features/branch/banchCellDivisionFrequencyAnimation.gif)                                                                                                                                                                                                                                                                                                          | ![](doc/features/branch/branchCellDivisionFrequency.png)                                                                              |
| Branch Relative Movement*            | x, y and z component | The x, y and z components of the average speed of a spot during its life span relative to its `n` nearest neighbors. The number of neighbors to be considered can be specified by the users. Default is 5.                                                                                                                                                                                                                                                                                                                                                                   | ![](doc/features/branch/branchRelativeMovementEquation1.gif)                                                                          |
|                                      | average speed        | The average speed of a spot during its life span relative to its `n` nearest neighbors. <br><br> Example visualization: ![branchRelativeMovementAnimation.gif](doc/features/branch/branchRelativeMovementAnimation.gif)                                                                                                                                                                                                                                                                                                                                                      | ![](doc/features/branch/branchRelativeMovementEquation2.gif)                                                                          |

'*' The relative movement features cannot be called from the FeatureComputer directly. Instead, they can be accessed via
the plugin menu: `Plugins > Compute Features > Movement of spots relative to nearest neighbors`

## Hierarchical Clustering of Lineage Trees

* Menu Location: `Plugins > Lineage Analysis > Hierarchical Clustering of Lineage Trees`
* This command is capable of grouping similar lineage trees together.
* The linage clustering operates on Mastodon's branch graph.
* Lineage trees are considered similar, if they share a similar structure and thus represent a similar cell division
  pattern. The structure of a lineage tree is represented by the tree topology.
  This tree topology consists of the actual branching pattern and the cell lifetimes,
  i.e. the time points between two subsequent cell divisions.
* Functionality in a nutshell: ![nutshell.gif](doc/clustering/nutshell.gif)

### Zhang Tree Edit Distance

* The similarity of a pair of lineage trees is computed based on the Zhang edit distance for unordered
  trees ([Zhang, K. Algorithmica 15, 205–222, 1996](https://doi.org/10.1007/BF01975866)). This method captures the cost
  of the transformation of one tree into the other.
* The Zhang unordered edit distance allows the following edit operations. The edit operations are defined in a way that
  they satisfy the constraints elaborated in section 3.1 ("Constrained Edit Distance Mappings") of the
  paper: [Zhang, K. Algorithmica 15, 205–222, 1996](https://doi.org/10.1007/BF01975866)

```
  Note: The prefix T may represent a node or a complete subtree. Nodes without this prefix are just nodes.
 
  1. Change label
 
        A         A'
       / \  -->  / \
      TB TC     TB TC
 
 
  2a: Delete subtree (opposite of 2b)
 
        A         A
       / \   -->  |
      TB TC       TB
 
  2b: Insert subtree (opposite of 2a)
 
        A          A
        |    -->  / \
        TB       TB TC
 
 
  3a: Delete one child of a node and delete the node itself (opposite of 3b)
 
        A             A
       / \   -->     / \
      B  TC         TD TC
     / \
    TD TE        (delete TE and B, TD becomes child of A)
 
  3b: Insert a node and insert one child at that node (opposite of 3a)
        A            A
       / \     -->  / \
      TB TC         D  TC
                   / \
                  TB TE       (insert D and TE, TB becomes child of D)
 
 
  4a: Delete node and delete its sibling subtree (opposite of 4b)
        A               A
       / \             / \
      B  TC   -->     TD TE
     / \
    TD TE            (Node B and its sibling subtree TC are deleted and the children
                      of B, namely TD and TE, become the children of A)
 
  4b: Insert node and insert a sibling subtree (opposite of 4a)
        A               A
       / \             / \
      TB TC   -->     D  TE
                     / \
                    TB TC       (Node D and its sibling TE are inserted,
                                 TB and TC become the children of D)
```

As an example, the following case explicitly does not fulfill the constraints mentioned in the paper:

```
 Delete a node without deleting one of its children
          A            A
         / \   -->   / | \
        B  TC      TD TE TC
       / \
      TD TE        (delete B, TD and TE become children of A and TC remains)
```

* A basic example of the tree edit distance:

```
Tree1
	                        node1(node_weight=13)
	               ┌──────────┴─────────────┐
	               │                        │
	             node2(node_weight=203)   node3(node_weight=203)
```

```
Tree2
	                        node1(node_weight=12)
	               ┌──────────┴─────────────┐
	               │                        │
	             node2(node_weight=227)   node3(node_weight=227)
	                             ┌──────────┴─────────────┐
	                           node4(node_weight=10)    node5(node_weight=10)
```

* Edit distance of 69, because:
    * one node has a difference of 1 (13-12)
    * two nodes have a difference of 24 each (227-203 * 2)
    * two extra nodes are added with a weight of 10 each (10 * 2)
        * ![zhang_example.gif](doc/clustering/zhang_example.gif)

### Workflow

1. The similarity measure uses the attribute cell lifetime, which is computed as a difference of time points between two
   subsequent divisions. There are multiple ways to compute the similarity between two lineage trees based on this
   attribute:
    1. The sum of the edit distances as shown in the basic example above. Individual differences in the cell lifetimes
       may be normalized by their sum (i.e. local normalization)
    2. The sum of the edit distances as shown in the basic example above normalized by the maximum possible edit
       distances of the two trees (normalized zhang edit distance)
    3. The sum of the edit distances normalized by the number of the involved nodes (per branch zhang edit distance)
2. The similarities are computed between all possible combinations of lineage trees leading to a two-dimensional
   similarity matrix. The values in this matrix are considered to reflect similarities of lineage trees. Low tree edit
   distances represent a high similarity between a discrete pair of lineage trees. This matrix is then used to perform
   an [Agglomerative Hierarchical Clustering](https://en.wikipedia.org/wiki/Hierarchical_clustering) into a specifiable
   number of groups.
3. For the clustering three
   different [linkage methods](https://en.wikipedia.org/wiki/Hierarchical_clustering#Cluster_Linkage) can be chosen.

### Parameters

* Crop criterion:
    * The criterion for cropping the lineage trees
    * Number of spots (default)
    * Time point
* Crop start
    * At which number of spots / time point (depending on the chose crop criterion) the analysis should start
* Crop end
    * At which number of spots / time point (depending on the chose crop criterion) the analysis should end
* Number of clusters
    * How many groups the lineage trees should be assigned to by the clustering
    * Must not be greater than the number of valid lineage trees
* Minimum number of divisions
    * The minimum number of divisions a lineage tree should have so that it is included in the analysis
* Similarity measures:
    1. (default) ![normalized_zhang_distance.gif](doc/clustering/normalized_zhang_distance.gif)<sup>1,2</sup>
    2. ![per_branch_zhang_distance.gif](doc/clustering/per_branch_zhang_distance.gif)<sup>1</sup>
    3. [Zhang](https://doi.org/10.1007/BF01975866) Tree Edit Distance<sup>1,2</sup>

    * <sup>1</sup>Local cost function: ![local_cost.gif](doc/clustering/local_cost.gif)
    * <sup>2</sup>Local cost function with
      normalization: ![local_cost_normalized.gif](doc/clustering/local_cost_normalized.gif)
* Linkage strategy for hierarchical clustering,
  cf. [linkage methods](https://en.wikipedia.org/wiki/Hierarchical_clustering#Cluster_Linkage)
    1. Average (default)
    2. Single
    3. Complete
* List of further projects
    * If you have multiple similar projects, you can add them here to get an average clustering taking all projects
      into account.
    * Mastodon projects can be added / removed using
        * "Add files..."
        * "Add folder content..."
        * "Remove selected"
        * "Clear list"
        * Drag and drop of files and folders
    * The name of the current open project is shown above the list. The current project is always included in the
      hierarchical clustering. It cannot be added to the list.
    * It is important that the names of the roots of lineages in all projects included in the hierarchical clustering
      are the same. Otherwise, the hierarchical clustering will not work.
    * The effect of adding further projects is that the similarity matrix is computed for each project separately and
      then averaged, resulting in a more robust hierarchical clustering.
* Add generated tags to further projects
    * If checked, the tags generated by the hierarchical clustering are also added to the further projects.
    * *Important note: this will write tags to these projects*. Consider making a backup of the further projects before
      running the hierarchical clustering, if you choose this option.
* Show dendrogram of hierarchical clustering of lineage trees
    * If checked, the dendrogram is shown after the hierarchical clustering
* Check validity of parameters
    * Press this button to check, if with the current parameters a hierarchical clustering is possible
    * If the parameters are invalid, a message will appear with the reason(s)
    * Possible reasons for invalid parameters:
        * The number of clusters is greater than the number of valid lineage trees
        * The crop start is greater than the crop end
        * The crop end is greater than the maximum number of spots / time points
        * Further projects that are included in the hierarchical clustering could not be found / opened

### Example

* Demo data: [Example data set](doc/clustering/lineage_clustering.mastodon)
    * The demo data does not contain any image data.
    * The spatial positions of the spots are randomly generated.
    * When opening the dataset, you should confirm that you open the project with dummy
      images. ![Dummy images](doc/clustering/dummy.png)
* The track scheme of the demo data contains 8 lineage tree in total. You may see that the "symmetric", the "asymmetric"
  and the "single division" trees look similar to each other, but dissimilar to the remaining
  trees. ![Trackscheme](doc/clustering/trackscheme.png)
* The hierarchical clustering dialog. ![Settings](doc/clustering/settings.png)
* Cf. section [Parameters](#parameters) for the meaning of the parameters.
* Not visible to the user, a similarity matrix is computed based on the chosen similarity measure. For the demo data,
  the matrix looks like this. Highly similar trees have low distances in this matrix.
    * ![Similarity matrix](doc/clustering/similarity_matrix.png)
* The resulting dendrogram.
    * User can toggle on/off root labels, tags, clustering threshold and median of the tree edit distances.
    * If the option `Show tag labels` is checked, the tag set shown in the dendrogram can be chosen.
    * Export options for the dendrogram to SVG and PNG accessible via the context menu.
    * ![Dendrogram](doc/clustering/dendrogram.png)
    * The result of the hierarchical clustering can be exported to a CSV file via the context menu. The exported file
      contains the root
      names of the lineage trees, the tag set value, the assigned group and the similarity score. The similarity score
      indicates how similar the lineage trees in this group are. The lower the score, the more similar the trees are.
    * ![Export clustering](doc/clustering/csv.png)
* The resulting tag set may be used for coloring the track
  scheme. ![Trackscheme with tags](doc/clustering/trackscheme_with_tags.png)
* The resulting tag set may be used for coloring the track scheme branch
  view. ![Trackscheme Branch View with tags](doc/clustering/trackscheme_branch_with_tags.png)
* The resulting tag set may be used for coloring the spots in the
  BigDataViewer. ![BigDataViewer](doc/clustering/bdv.gif)

## Dimensionality reduction

For visualizing high-dimensional data, e.g. in two dimensions, potentially getting more insights into your data, you can
reduce the dimensionality of the measurements, using this algorithm:

* [Uniform Manifold Approximation Projection (UMAP)](https://arxiv.org/abs/1802.03426)
* [UMAP Python implementation](https://umap-learn.readthedocs.io/en/latest/)

### Usage

* Menu Location: `Plugins > Compute Feature > Dimensionality reduction > UMAP`

Select the graph type whose features should be dimensionality reduced, either the Model Graph with Features for Spots
and Links or the Branch Graph with Features on BranchSpots and BranchLinks.
Next, select the feature + feature projections that should be dimensionality reduced. Prefer to select features, which
describe the phenotype (e.g. size, shape, velocity, number of neighbors, etc.).
Only select positional features (e.g. centroid, coordinates, timeframe, etc.) if the position of cells within
the image are descriptive for the phenotype. If you are unsure, you can select all features and then remove the
positional features later.

### Description

The UMAP algorithm reduces the dimensionality of the selected features and adds the reduced features to the table.
In order to do so, the UMAP algorithm uses the data matrix from the spot or branch spot table, where each row represents
a spot or branch spot and each column represents a feature. The link and branch link features can be included in the
algorithm.

If they are selected, the algorithm will use the link feature value of its incoming edge or the average of all values of
all incoming edges, if there is more than one incoming edge.

The dialog will look like this:
![umap_dialog.png](doc/dimensionalityreduction/umap_dialog.png)

By default, all measurements are selected in the box.

### Parameters

* Standardize: Whether to standardize the data before reducing the dimensionality. Standardization is recommended when
  the data has different scales / units.
  Further
  reading: [Standardization](https://scikit-learn.org/stable/modules/preprocessing.html#standardization-or-mean-removal-and-variance-scaling).
* Number of dimensions: The number of reduced dimensions to use. The default is 2, but 3 is also common.
  Further reading: [Number of Dimensions](https://umap-learn.readthedocs.io/en/latest/parameters.html#n-components).
* Number of neighbors: The size of the local neighborhood (in terms of number of neighboring sample points) used for
  manifold approximation.
  Larger values result in more global views of the manifold, while smaller values result in more local data being
  preserved.
  In general, it should be in the range 2 to 100.
  Further reading: [Number of Neighbors](https://umap-learn.readthedocs.io/en/latest/parameters.html#n-neighbors).
* Minimum distance: The minimum distance that points are allowed to be apart from each other in the low dimensional
  representation. This parameter controls how tightly UMAP is allowed to pack points together.
  Further reading: [Minimum Distance](https://umap-learn.readthedocs.io/en/latest/parameters.html#min-dist).

When you are done with the selection, click on `Compute UMAP`.
The resulting values will be added as additional columns to the selected table.

![umap_table.png](doc/dimensionalityreduction/umap_table.png)

You can visualize the results using the `Grapher` View of Mastodon and selecting the newly added columns.

![umap_grapher.gif](doc/dimensionalityreduction/umap_grapher.gif)

Visualization with the [Mastodon Blender View](https://github.com/mastodon-sc/mastodon-blender-view) is also possible.

![umap_blender.gif](doc/dimensionalityreduction/umap_blender.gif)

### Example

The example above has been generated using the [
tgmm-mini](https://github.com/mastodon-sc/mastodon-example-data/tree/master/tgmm-mini) dataset, which is included in
the Mastodon repository.

## Import

### Import Spots from Segmented Label Image

* Menu Location: `File > Import > Import spots from label image`
* You can use the plugin to import spots from a label image representing an instance segmentation into Mastodon. This
  may be useful if you have an instance segmentation of cells or other objects, and you want to track them using
  Mastodon.
* The label image is expected to contain the spot ids as pixel values.
* The label image is expected to have the same dimensions as the image data in Mastodon.
* Labels are processed frame by frame.
* Multiple blobs with the same id in the same frame are considered to belong to the same spot by this importer. It is
  advised to use unique ids for spots in the same frame.
* The resulting spots are ellipsoids with the semi axes computed from the variance covariance matrix of this pixel
  positions of each label.
* Labels with only one pixel are ignored. This is because the variance covariance matrix is not defined for a single
  point. If you want to import single pixel spots, you can use the [
  `Import Spots from CSV`](https://mastodon.readthedocs.io/en/latest/docs/partC/csv-importer.html) plugin.
* The resulting spots may be linked using the linker plugin in Mastodon (`Plugins > Tracking > Linking...`)
  or [Elephant](https://elephant-track.github.io/#/?id=linking-workflow).

#### Parameters

* Ellipsoid scaling factor: The scaling factor to apply to the ellipsoids. The default is 1.0. The scaling factor is
  applied to the semi axes of the ellipsoids. The ellipsoid scaling factor can be used to increase (>1) or decrease (
  &lt;1) the size of the resulting ellipsoid. 1 is equivalent of ellipsoids drawn at 2.2σ.
* Link spots having the same id in consecutive frames: If checked, spots with the same label id in consecutive frames
  are linked. Division or merge events are not considered.
* Image source that has been used for the external segmentation: The channel containing the image data that has been
  used to create the label image.
  This channel is used to check, if the dimensions of the label image match the dimensions of the image data in
  Mastodon.

#### Label image as active image in ImageJ

* The label image can be opened in ImageJ and the plugin can be called from the
  menu: `File > Import > Import spots from label image > Import spots from ImageJ image`
* Please make sure that the label image is the active image in ImageJ.
* Please make sure that the label image has the same dimensions as the image data in Mastodon.
    * You can use the `Image > Properties` command ImageJ to check (and) set the dimensions of the label image.

##### Example

* You can also watch a video tutorial on how to import spots from a label image in
  Mastodon [![YouTube](https://badges.aleen42.com/src/youtube.svg)](https://www.youtube.com/watch?v=kQakhhBnl_8)
* Example
  dataset: [Fluo-C3DL-MDA231 from Cell Tracking Challenge](http://data.celltrackingchallenge.net/training-datasets/Fluo-C3DL-MDA231.zip)
* Extract the file to a folder named `Fluo-C3DL-MDA231`
* Import the image sequence with the actual image into ImageJ contained in folder `Fluo-C3DL-MDA231/01/`
    * `File > Import > Image Sequence...`
* Set the dimensions of the image sequence to 512x512x1x30x12 (XYCTZ) using `Image > Properties`
    * ![plugin_import_example_1.png](doc/import/label_image/plugin_import_example_01.png)
* Open Mastodon from Fiji and create a new project with the image sequence
    * `Plugins > Mastodon > new Mastodon project > Use an image opened in ImageJ > Create`
  * ![plugin_import_example_2.png](doc/import/label_image/plugin_import_example_02.png)
* Import the image sequence encoding the label images into ImageJ contained in folder: `Fluo-C3DL-MDA231/01_ERR_SEG/`
    * Set the dimensions of the label image to 512x512x1x30x12 (XYCTZ) using `Image > Properties`
  * ![plugin_import_example_3.png](doc/import/label_image/plugin_import_example_03.png)
* Open Import window in Mastodon: `File > Import > Import spots from label image > Import spots from ImageJ image`
    * You can keep the ellipsoid scaling factor at 1.0. Select factor higher than 1.0 to increase the size of the
      ellipsoids and lower than 1.0 to decrease the size of the resulting ellipsoids.
    * Check the box to link spots having the same label id in consecutive frames.
    * Select the channel in Big Data Viewer containing the image that has been used to create the label image. The
      channel is used to check, if the dimensions of the label image in ImageJ match the dimensions of the image data in
      Mastodon.
  * ![plugin_import_example_8.png](doc/import/label_image/plugin_import_example_08.png)
    * Click `OK` and the spots are imported into Mastodon.
  * ![plugin_import_example_4.png](doc/import/label_image/plugin_import_example_04.png)

#### Label image as BDV channel

* The plugin can be called from the
  menu: `File > Import > Import spots from label image > Import spots from BDV channel`

##### Example

* You can also watch a video tutorial on how to import spots from a label image in
  Mastodon [![YouTube](https://badges.aleen42.com/src/youtube.svg)](https://www.youtube.com/watch?v=kQakhhBnl_8)
* Example
  dataset: [Fluo-C3DL-MDA231 from Cell Tracking Challenge](http://data.celltrackingchallenge.net/training-datasets/Fluo-C3DL-MDA231.zip)
* Extract the file to a folder named `Fluo-C3DL-MDA231`
* Import the image sequence with the actual image into ImageJ contained in folder `Fluo-C3DL-MDA231/01/`
    * `File > Import > Image Sequence...`
    * Set the dimensions of the image sequence to 512x512x1x30x12 (XYCTZ) using `Image > Properties`
  * ![plugin_import_example_1.png](doc/import/label_image/plugin_import_example_01.png)
* Import the image sequence encoding the label images into ImageJ contained in folder: `Fluo-C3DL-MDA231/01_ERR_SEG/`
    * Set the dimensions of the label image to 512x512x1x30x12 (XYCTZ) using `Image > Properties`
  * ![plugin_import_example_3.png](doc/import/label_image/plugin_import_example_03.png)
    * Merge the 2 images into a single image using the `Image > Color > Merge Channels...` command
  * ![plugin_import_example_5.png](doc/import/label_image/plugin_import_example_05.png)
* Open Mastodon from Fiji and create a new project with merged image
    * `Plugins > Mastodon > new Mastodon project > Use an image opened in ImageJ > Create`
  * ![plugin_import_example_6.png](doc/import/label_image/plugin_import_example_06.png)
* Open Import window: `File > Import > Import spots from label image > Import spots from BDV channel`
    * Keep the ellipsoid scaling factor at 1.0
    * You can decide to link spots having the same label id in consecutive frames. This is useful if you have a time
      series of label images and you want to link spots between frames. Linking dividing spots cannot be done by this.
      The Mastodon Linker plugin should be used for this.
    * Select the BDV channel containing the label image that has been used to create the segmented label image. This is
      used to check, if the dimensions of the label image and the image data in BDV match, which is required.
  * ![plugin_import_example_9.png](doc/import/label_image/plugin_import_example_08.png)
    * Click `OK` and the spots are imported into Mastodon.
  * ![plugin_import_example_7.png](doc/import/label_image/plugin_import_example_07.png)

## Export

### Label Image Export

* Menu Location: `File > Export > Export label image using ellipsoids`
* The Label image exporter is capable of saving a label image to a file using the existing ellipsoids in Mastodon.
* For the labels, the _spot ids_, _branch spot ids_ or the _track ids_ that correspond to the spots / ellipsoids may be
  used. Since these Ids are counted zero based in Mastodon, an **offset of 1** is added to all Ids so that no label
  clashes with the background of zero.
* The recommended export format is '*.tif'-files. However, it should work also for other formats supported by ImageJ.
* The export uses an image with signed integer value space, thus the maximum allowed id is 2.147.483.646.
* The dialog:  ![Plugin Export Dialog](doc/export/label_image/plugin_export_dialog.png)

#### Parameters

* Label Id: The id that is used for the labels. The default is the Spot track Id.
    * The ids correspond to the highlighted columns in the feature
      table: ![Feature Table](doc/export/label_image/plugin_export_table.png)
* Frame rate reduction: Only export every n-th frame. 1 means no reduction. Value must be >= 1.
    * The frame number corresponds to the _Spot frame_ column in the feature table.
* Resolution level: Spatial resolution level of export. 0 means highest resolution. Value > 0 mean lower resolution.
* Save to: Path to the file to save the label image to. Should end with '.tif'.

#### Example

* Demo data: [Example data set](https://github.com/mastodon-sc/mastodon-example-data/tree/master/tgmm-mini)
* The timelapse with the ellipsoids in
  BigDataViewer: ![BigDataViewer](doc/export/label_image/bdv_timelapse.gif)
* The exported tif imported into [Napari](https://napari.org/stable/) 3D
  view: ![Napari](doc/export/label_image/napari_timelapse.gif)

### GraphML Export

* Menu Location: `File > Export > Export to GraphML (branches)`
* Exports the branch graph to a [GraphML](http://graphml.graphdrawing.org/) file.
    * The graph is directed. The branch spots are the vertices and the branch links are the edges.
    * The vertices receive a label attribute with the branch spot name. The vertices receive a duration attribute with
      the branch duration.
    * The edges are not labeled and have no attributes.
* GraphML can be visualized with [Cytoscape](https://cytoscape.org/), [yEd](https://www.yworks.com/products/yed)
  or [Gephi](https://gephi.org/).
* GraphML can be processed in Java using the [JGraphT](https://jgrapht.org/) library.
* GraphML can be processed in Python using the [NetworkX](https://networkx.org/) library.

#### Options

* Export all branches to GraphML (one file)
    * Exports the whole branch graph to a single file.
    * Select a file to save to. Should end with '.graphml'.
* Export selected branches to GraphML (one file)
    * Exports the selected branches to a single file.
        * The selected branches are the ones that are highlighted in the branch view.
        * A branch is considered selected if at least one of its spots is selected. However, the exported duration
          attribute
          always reflects the whole branch duration.
    * Select a file to save to. Should end with '.graphml'.
* Export tracks to GraphML (one file per track)
    * Exports each track to a separate file.
    * Select a directory to save to.

#### Example

* Demo data: [Example data set](https://github.com/mastodon-sc/mastodon-example-data/tree/master/tgmm-mini)
* The resulting file loaded into yEd: ![yEd](doc/export/graphml/yed_graphml.png)
* The resulting file loaded into Cytoscape: ![Cytoscape](doc/export/graphml/cytoscape_graphml.png)

## Technical Information

### Maintainer

* [Stefan Hahmann](https://github.com/stefanhahmann/)

### Contributors

* [Matthias Arzt](https://github.com/maarzt/)

### License

* [BSD 2-Clause License](https://opensource.org/license/bsd-2-clause/)

### Contribute Code or Provide Feedback

* You are welcome to submit Pull Requests to this repository. This repository runs code analyses on
  every Pull Request using [SonarCloud](https://sonarcloud.io/dashboard?id=mastodon-sc_mastodon-deep-lineage).
* Please read the [general advice](https://github.com/mastodon-sc/) re contributing to Mastodon and its plugins.

### Contribute Documentation

* If you would like to contribute to this documentation, feel free to open a pull request. The documentation is written
  in Markdown format.

## Acknowledgements

* The development of this plugin was supported by the [DFG](https://www.dfg.de/en/) under
  grant [490966236](https://gepris.dfg.de/gepris/projekt/490966236) and the [ANR](https://anr.fr/en/) under
  grant [ANR-21-CE13-0044](https://anr.fr/Project-ANR-21-CE13-0044).
