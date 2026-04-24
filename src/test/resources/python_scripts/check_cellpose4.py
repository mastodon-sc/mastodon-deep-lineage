"""
Smoke test for the Cellpose 4 environment generated from cellpose4.toml.

What it verifies:
  1. torch, cellpose, numpy import cleanly
  2. Reports the accelerator available (CUDA / MPS / CPU)
  3. Loads the default Cellpose 4 model (cpsam)
  4. Runs segmentation on a synthetic 2D image with known blobs
  5. Asserts at least one mask was produced

Exit codes:
  0 - env is healthy, segmentation ran
  1 - one of the checks failed

Usage:
  # Using the Appose-built env (as created by mastodon-deep-lineage):
  ~/.local/share/appose/cellpose4/bin/python scripts/check_cellpose4.py

  # Using a standalone pixi env from cellpose4.toml:
  pixi run --manifest-path src/main/resources/org/mastodon/mamut/detection/cellpose/cellpose4.toml \\
      python scripts/check_cellpose4.py
"""
import sys
import platform

import numpy as np


def make_synthetic_image(size=256, n_blobs=9, radius=15):
    """Grid of filled circles on a noisy background — easy for cellpose."""
    rng = np.random.default_rng(42)
    img = rng.normal(loc=100, scale=5, size=(size, size)).astype(np.float32)
    step = size // (int(np.sqrt(n_blobs)) + 1)
    yy, xx = np.mgrid[0:size, 0:size]
    for i in range(1, int(np.sqrt(n_blobs)) + 1):
        for j in range(1, int(np.sqrt(n_blobs)) + 1):
            cy, cx = i * step, j * step
            mask = (yy - cy) ** 2 + (xx - cx) ** 2 <= radius ** 2
            img[mask] += 200
    return img


def pick_device():
    import torch
    if torch.cuda.is_available():
        return "cuda", True
    if getattr(torch.backends, "mps", None) and torch.backends.mps.is_available():
        return "mps", False  # cellpose gpu= flag checks CUDA; MPS is opportunistic
    return "cpu", False


def main() -> int:
    print(f"Python         : {sys.version.split()[0]} ({platform.platform()})")

    try:
        import torch
        import cellpose
        from cellpose import models
    except Exception as e:
        print(f"FAIL import: {e}")
        return 1

    print(f"numpy          : {np.__version__}")
    print(f"torch          : {torch.__version__}")
    print(f"cellpose       : {cellpose.version}")

    device, use_cuda = pick_device()
    print(f"accelerator    : {device}")

    img = make_synthetic_image()
    print(f"input image    : shape={img.shape} dtype={img.dtype}")

    try:
        model = models.CellposeModel(gpu=use_cuda)
    except Exception as e:
        print(f"FAIL model load: {e}")
        return 1

    try:
        masks, flows, styles = model.eval(
            img,
            diameter=30,
            do_3D=False,
            z_axis=None,
            normalize=True,
            batch_size=8,
            flow_threshold=0.4,
            cellprob_threshold=0.0,
        )
    except Exception as e:
        print(f"FAIL segmentation: {e}")
        return 1

    n_masks = int(masks.max())
    print(f"masks found    : {n_masks}")
    if n_masks < 1:
        print("FAIL: expected at least 1 mask, got 0")
        return 1

    print("OK: env is healthy and cellpose segmentation works.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
