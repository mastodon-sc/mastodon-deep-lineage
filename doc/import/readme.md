## Imports

### Import Spots from Label Image

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
  point. If you want to import single pixel spots, you can use the `Import Spots from CSV` plugin.
* The resulting spots may be linked using the linker plugin in Mastodon (`Plugins > Tracking > Linking...`)
  or [Elephant](https://elephant-track.github.io/#/?id=linking-workflow).

#### Parameters

* Ellipsoid scaling factor: The scaling factor to apply to the ellipsoids. The default is 1.0. The scaling factor is
  applied to the semi axes of the ellipsoids. The ellipsoid scaling factor can be used to increase (>1) or decrease (
  &lt;1) the size of the resulting ellipsoid. 1 is equivalent of ellipsoids drawn at 2.2σ.
* Link spots having the same id in consecutive frames: If checked, spots with the same id in consecutive frames are
  linked.

#### Instance segmentation as active image in ImageJ

* The label image can be opened in ImageJ and the plugin can be called from the
  menu: ![Plugin Import Menu](label_image/imagej/plugin_import_imagej_menu.png)
* Please make sure that the label image is the active image in ImageJ.
* Please make sure that the label image has the same dimensions as the image data in Mastodon.
    * You can use the `Image > Properties` command ImageJ to check (and) set the dimensions of the label image.

##### Example

* Example
  dataset: [Fluo-C3DL-MDA231 from Cell Tracking Challenge](http://data.celltrackingchallenge.net/training-datasets/Fluo-C3DL-MDA231.zip)
    * Extract the file to a folder named `Fluo-C3DL-MDA231`
    * Import the image sequence with the actual image into ImageJ contained in folder `Fluo-C3DL-MDA231/01/`
        * `File > Import > Image Sequence...`
    * Set the dimensions of the image sequence to 512x512x1x30x12 (XYCTZ) using `Image > Properties`
    * ![plugin_import_example_1.png](label_image/imagej/plugin_import_example_1.png)
    * Open Mastodon from Fiji and create a new project with the image sequence
        * `Plugins > Mastodon > new Mastodon project > Use an image opened in ImageJ > Create`
        * ![plugin_import_example_2.png](label_image/imagej/plugin_import_example_2.png)
    * Import the image sequence encoding the label images into ImageJ contained in
      folder: `Fluo-C3DL-MDA231/01_ERR_SEG/`
    * Set the dimensions of the label image to 512x512x1x30x12 (XYCTZ) using `Image > Properties`
        * ![plugin_import_example_3.png](label_image/imagej/plugin_import_example_3.png)
    * Open Import window in
      Mastodon: `Plugins > Imports > Import spots from label image > Import spots from ImageJ image`
  * Select the channel in Big Data Viewer containing the image that has been used to create the label image.
    * Click `OK` and the spots are imported into Mastodon.
        * ![plugin_import_example_4.png](label_image/imagej/plugin_import_example_4.png)

#### Instance segmentation as BDV channel

* The plugin can be called from the
  menu: ![Plugin Import Menu](label_image/imagej/plugin_import_imagej_menu.png)

##### Example

* Example
  dataset: [Fluo-C3DL-MDA231 from Cell Tracking Challenge](http://data.celltrackingchallenge.net/training-datasets/Fluo-C3DL-MDA231.zip)
    * Extract the file to a folder named `Fluo-C3DL-MDA231`
    * Import the image sequence with the actual image into ImageJ contained in folder `Fluo-C3DL-MDA231/01/`
        * `File > Import > Image Sequence...`
    * Set the dimensions of the image sequence to 512x512x1x30x12 (XYCTZ) using `Image > Properties`
    * ![plugin_import_example_1.png](label_image/imagej/plugin_import_example_1.png)
    * Import the image sequence encoding the label images into ImageJ contained in
      folder: `Fluo-C3DL-MDA231/01_ERR_SEG/`
    * Set the dimensions of the label image to 512x512x1x30x12 (XYCTZ) using `Image > Properties`
        * ![plugin_import_example_3.png](label_image/imagej/plugin_import_example_3.png)
    * Merge the 2 images into a single image using the `Image > Color > Merge Channels...` command
        * ![plugin_import_example_5.png](label_image/imagej/plugin_import_example_5.png)
    * Open Mastodon from Fiji and create a new project with merged image
        * `Plugins > Mastodon > new Mastodon project > Use an image opened in ImageJ > Create`
        * ![plugin_import_example_6.png](label_image/imagej/plugin_import_example_6.png)
    * Open Import window: `Plugins > Imports > Import spots from label image > Import spots from BDV channel`
    * Select the BDV channel containing the label image
    * Click `OK` and the spots are imported into Mastodon.
        * ![plugin_import_example_7.png](label_image/imagej/plugin_import_example_7.png)
