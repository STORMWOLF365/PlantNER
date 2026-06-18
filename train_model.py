import os
import time
import argparse
from pathlib import Path

import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers

# Default fallback if argument is not specified
DEFAULT_DATASET_DIR = Path("./plantvillage_dataset")
SEED = 42
BATCH_SIZE = 64
EPOCHS = 15
IMG_SIZE = (128, 128)
VALIDATION_AND_TEST_SPLIT = 0.30
IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".bmp"}


def child_has_images(directory: Path) -> bool:
    """Check if directory contains images matching the set extensions."""
    try:
        return any(
            file.is_file() and file.suffix.lower() in IMAGE_EXTENSIONS
            for file in directory.iterdir()
        )
    except OSError:
        return False


def find_image_class_directory(root: Path) -> Path:
    """Find the directory whose child folders are image classes."""
    candidates = []

    for current_root, child_dirs, _ in os.walk(root):
        current_path = Path(current_root)
        class_count = sum(
            child_has_images(current_path / child_name) for child_name in child_dirs
        )
        if class_count >= 2:
            extra_folder_count = len(child_dirs) - class_count
            candidates.append((class_count, -extra_folder_count, current_path))

    if not candidates:
        raise FileNotFoundError(
            f"Could not find any directory with at least 2 class subfolders containing images under root: {root}\n"
            f"Please verify that the dataset is unpacked and contains class folders (e.g., Tomato_Blight, Healthy_Basil)."
        )

    candidates.sort(key=lambda item: (item[0], item[1]), reverse=True)
    return candidates[0][2]


def load_plantvillage_datasets(image_root: Path):
    """Load train, validation, and test datasets with a split from directory."""
    print(f"Loading datasets from: {image_root}")
    train_ds = keras.utils.image_dataset_from_directory(
        image_root,
        validation_split=VALIDATION_AND_TEST_SPLIT,
        subset="training",
        seed=SEED,
        image_size=IMG_SIZE,
        batch_size=BATCH_SIZE,
        label_mode="int",
    )

    holdout_ds = keras.utils.image_dataset_from_directory(
        image_root,
        validation_split=VALIDATION_AND_TEST_SPLIT,
        subset="validation",
        seed=SEED,
        image_size=IMG_SIZE,
        batch_size=BATCH_SIZE,
        label_mode="int",
    )

    class_names = train_ds.class_names
    holdout_batches = tf.data.experimental.cardinality(holdout_ds).numpy()
    validation_batches = holdout_batches // 2

    val_ds = holdout_ds.take(validation_batches)
    test_ds = holdout_ds.skip(validation_batches)

    # Enable prefetching for hardware capability acceleration
    autotune = tf.data.AUTOTUNE
    train_ds = train_ds.prefetch(autotune)
    val_ds = val_ds.prefetch(autotune)
    test_ds = test_ds.prefetch(autotune)

    return train_ds, val_ds, test_ds, class_names


def build_model(num_classes: int) -> keras.Model:
    """Build a Convolutional Neural Network (CNN) model for leaf diagnosis."""
    data_augmentation = keras.Sequential(
        [
            layers.RandomFlip("horizontal"),
            layers.RandomRotation(0.08),
            layers.RandomZoom(0.10),
        ],
        name="data_augmentation",
    )

    model = keras.Sequential(
        [
            layers.Input(shape=IMG_SIZE + (3,)),
            data_augmentation,
            layers.Rescaling(1.0 / 255),
            layers.Conv2D(32, (3, 3), activation="relu"),
            layers.MaxPooling2D(pool_size=(2, 2)),
            layers.Conv2D(64, (3, 3), activation="relu"),
            layers.MaxPooling2D(pool_size=(2, 2)),
            layers.Conv2D(128, (3, 3), activation="relu"),
            layers.MaxPooling2D(pool_size=(2, 2)),
            layers.Conv2D(128, (3, 3), activation="relu"),
            layers.MaxPooling2D(pool_size=(2, 2)),
            layers.Flatten(),
            layers.Dense(128, activation="relu"),
            layers.Dropout(0.3),
            layers.Dense(num_classes, activation="softmax"),
        ]
    )

    model.compile(
        loss=keras.losses.SparseCategoricalCrossentropy(),
        optimizer=keras.optimizers.Adam(),
        metrics=["accuracy"],
    )

    return model


def main():
    parser = argparse.ArgumentParser(description="Train custom CNN classifier on PlantVillage dataset.")
    parser.add_argument(
        "--dataset_path",
        type=str,
        default=os.getenv("PLANTVILLAGE_DIR", str(DEFAULT_DATASET_DIR)),
        help="Path containing class-specific folders representing crop symptoms and disease variants."
    )
    parser.add_argument(
        "--epochs",
        type=int,
        default=EPOCHS,
        help="Number of epochs to train the neural network."
    )
    parser.add_argument(
        "--convert_tflite",
        type=bool,
        default=True,
        help="If True, automatically exports the trained model as a compact .tflite unit for the phone."
    )
    
    args = parser.parse_args()
    dataset_path = Path(args.dataset_path).expanduser()

    if not dataset_path.exists():
        print(f"\n[!] WARNING: Dataset path does not exist: {dataset_path}")
        print("Creating placeholder folders so the project directory hierarchy is clear.")
        dataset_path.mkdir(parents=True, exist_ok=True)
        (dataset_path / "Tomato_Blight").mkdir(exist_ok=True)
        (dataset_path / "Healthy_Basil").mkdir(exist_ok=True)
        print(f"Please extract the Kaggle PlantVillage images under: {dataset_path.absolute()}")
        return

    try:
        image_root = find_image_class_directory(dataset_path)
    except FileNotFoundError as e:
        print(f"\n[!] Error: {e}")
        return

    print("\n[+] Found diagnostic image classes in directory:", image_root)
    train_ds, val_ds, test_ds, class_names = load_plantvillage_datasets(image_root)

    print("[+] Number of distinct diagnostic labels:", len(class_names))
    print("[+] Diagnostic categories:", class_names)

    # Save class names mapping as txt helper
    classes_file = Path("./classes.txt")
    with open(classes_file, "w") as f:
        for name in class_names:
            f.write(f"{name}\n")
    print(f"[+] Saved diagnostic labels to text file helper: {classes_file.absolute()}")

    model = build_model(num_classes=len(class_names))
    model.summary()

    print(f"\n[*] Starting training loop. Epoch size: {args.epochs}. Batch Size: {BATCH_SIZE}...")
    start_time = time.time()
    model.fit(train_ds, validation_data=val_ds, epochs=args.epochs)
    end_time = time.time()
    
    elapsed = end_time - start_time
    print(f"[+] Model training completed in {elapsed:.2f} seconds.")

    print("\n[*] Running test set evaluation metrics...")
    test_loss, test_acc = model.evaluate(test_ds)
    print(f"[+] Evaluation Complete. Test Loss: {test_loss:.4f} | Test Accuracy: {test_acc:.4f}")

    # Predict single test sample to verify pipeline stability
    for images, labels in test_ds.take(1):
        predictions = model.predict(images)
        sample_index = 0
        predicted_class_idx = int(np.argmax(predictions[sample_index]))
        actual_class_idx = int(labels[sample_index].numpy())

        print("\n--- Diagnostic Pipeline Validation Check ---")
        print("Predicted Class Label:", class_names[predicted_class_idx])
        print("Actual Class Label:   ", class_names[actual_class_idx])
        print("--------------------------------------------\n")
        break

    # Save absolute model
    keras_model_path = Path("./plantner_leaf_classifier.keras")
    model.save(keras_model_path)
    print(f"[+] Saved full Keras baseline trained design structure to: {keras_model_path.absolute()}")

    # Automated convert-stage to TFLite
    if args.convert_tflite:
        print("\n[*] Initializing TensorFlow Lite Conversion pipeline for mobile deployment...")
        converter = tf.lite.TFLiteConverter.from_keras_model(model)
        
        # Optimize size (Quantization optimization flag)
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        
        tflite_model = converter.convert()
        
        tflite_file_path = Path("./plantner_leaf_classifier.tflite")
        with open(tflite_file_path, "wb") as f:
            f.write(tflite_model)
        print(f"[+] Success! Exported quantized compact mobile model to: {tflite_file_path.absolute()}")
        print("[*] Place this file in the 'assets' directory of your Android Project to run client-side offline scans.")


if __name__ == "__main__":
    main()
