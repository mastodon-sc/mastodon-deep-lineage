import pandas as pd


def read_csv_to_dataframe(file_path):
    """
    Reads a CSV file and returns it as a pandas DataFrame.

    Parameters:
        file_path (str): Path to the CSV file.

    Returns:
        DataFrame: The loaded DataFrame.
    """
    try:
        # Read the CSV file using pandas
        df = pd.read_csv(file_path, sep=',')
        print("CSV file successfully loaded!")
        print(f"Number of rows: {df.shape[0]}")
        return df
    except FileNotFoundError:
        print("Error: The file was not found.")
    except Exception as e:
        print(f"An error occurred: {e}")

def read_tsv_to_dataframe(file_path):
    """
    Reads a TSV file and returns it as a pandas DataFrame.

    Parameters:
        file_path (str): Path to the TSV file.

    Returns:
        DataFrame: The loaded DataFrame.
    """
    try:
        # Read the CSV file using pandas
        df = pd.read_csv(file_path, sep='\t')
        print("CSV file successfully loaded!")
        print(f"Number of rows: {df.shape[0]}")
        return df
    except FileNotFoundError:
        print("Error: The file was not found.")
    except Exception as e:
        print(f"An error occurred: {e}")


# Example usage
if __name__ == "__main__":

    # Perform UMAP and plot the data
    import umap.umap_ as umap
    import numpy as np
    import matplotlib.pyplot as plt
    from sklearn.preprocessing import StandardScaler

    reducer = umap.UMAP(
        metric="euclidean",
        random_state=42,
        n_components=2,
        min_dist=0.1,
        n_neighbors=15,
        learning_rate=1.0,
        local_connectivity=1.0,
        negative_sample_rate=5,
        repulsion_strength=1.0,
        n_jobs=1,
        set_op_mix_ratio=1.0,
        spread=1.0,
        target_n_neighbors=-1,
        target_weight=0.5,
        transform_queue_size=4.0,
        angular_rp_forest=False,
        transform_seed=42,
        n_epochs=500,
        target_metric="categorical"
    )

    dataframe = read_tsv_to_dataframe("iris.tsv")
    column0 = dataframe.iloc[:, 0].to_numpy()
    column1 = dataframe.iloc[:, 1].to_numpy()
    column2 = dataframe.iloc[:, 2].to_numpy()
    column3 = dataframe.iloc[:, 3].to_numpy()
    column4 = dataframe.iloc[:, 4].to_numpy()
    dataframe2 = np.array([column1, column2, column3, column4]).T
    dataframe2 = StandardScaler().fit_transform(dataframe2)
    umap = reducer.fit_transform(dataframe2)

    umap2 = umap[:, 0]
    umap1 = -umap[:, 1]

    # Plot the data
    plt.figure(figsize=(8, 6))
    plt.plot(umap1, umap2, marker='o', markersize=1, linestyle='', color='orange', label=f"UMAP1 vs UMAP2")
    plt.xlabel("UMAP1")
    plt.ylabel("UMAP2")
    plt.title(f"Plot")
    plt.legend()
    plt.grid(True)
    plt.show()

    file_path = "tgmm-mini-spot.csv"  # Replace with the path to your CSV file
    dataframe = read_csv_to_dataframe(file_path)
    if dataframe is not None:
        print(dataframe.head())  # Display the first few rows of the DataFrame

    # Perform PCA and plot the data
    from sklearn.decomposition import PCA


    pca_object = PCA(n_components=2)
    scaled_dataframe = StandardScaler().fit_transform(dataframe)
    pca_transformed_dataframe = pca_object.fit_transform(scaled_dataframe)

    pca1 = pca_transformed_dataframe[:, 0]
    pca2 = pca_transformed_dataframe[:, 1]

    # Plot the data
    plt.figure(figsize=(8, 6))
    plt.plot(pca1, pca2, marker='o', markersize=1, linestyle='', color='b', label=f"PCA1 vs PCA2")
    plt.xlabel("PCA1")
    plt.ylabel("PCA2")
    plt.title(f"Plot")
    plt.legend()
    plt.grid(True)
    plt.show()

    # Perform t-SNE and plot the data
    from sklearn.manifold import TSNE

    reducer = TSNE(
        perplexity=30,
        n_components=2,
        learning_rate="auto",
        init="pca",
        random_state=42,
    )
    tsne_transformed_dataframe = reducer.fit_transform(scaled_dataframe)

    tsne1 = tsne_transformed_dataframe[:, 0]
    tsne2 = tsne_transformed_dataframe[:, 1]

    # Plot the data
    plt.figure(figsize=(8, 6))
    plt.plot(tsne1, tsne2, marker='o', markersize=1, linestyle='', color='r', label=f"t-SNE1 vs t-SNE2")
    plt.xlabel("t-SNE1")
    plt.ylabel("t-SNE2")
    plt.title(f"Plot")
    plt.legend()
    plt.grid(True)
    plt.show()

    umap_transformed_dataframe = reducer.fit_transform(scaled_dataframe)

    umap1 = umap_transformed_dataframe[:, 0]
    umap2 = umap_transformed_dataframe[:, 1]

    # Plot the data
    plt.figure(figsize=(8, 6))
    plt.plot(umap1, umap2, marker='o', markersize=1, linestyle='', color='g', label=f"UMAP1 vs UMAP2")
    plt.xlabel("UMAP1")
    plt.ylabel("UMAP2")
    plt.title(f"Plot")
    plt.legend()
    plt.grid(True)
    plt.show()
