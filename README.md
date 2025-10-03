# 🌱 Crop Disease Detection Android App

An AI-powered **Android application** designed to help farmers detect crop diseases using **leaf images**. The app uses an optimized **MobileNet** deep learning model for real-time classification of plant diseases directly on the device, reducing the need for constant internet connectivity.

## App Demo 
[**Demo link**](https://www.youtube.com/shorts/sz0jUWNwaAs)

## Features
- **On-Device AI**: Runs inference locally on mobile devices using **MobileNet**, ensuring speed and offline accessibility.  
- **Crop Disease Detection**: Classifies crop leaf images into multiple disease categories.  
- **87K+ Image Dataset**: Model trained on an augmented dataset, achieving **90%+ classification accuracy**.  
- **Optimized for Mobile**: Lightweight, efficient, and suitable for real-time use in agricultural fields.  
- **Firebase Integration**: For user authentication.  
- **Object Detection Support**: Integrated **YOLOv8** for leaf localization in images, helping reduce misclassification.

## App screenshot
<table>
  <tr>
    <td>
      <img width="200" height="600" alt="brief" src="https://github.com/RadhapyariDevi/Crop-Disease-Detection-AI/blob/main/Screenshots/homepage.jpg" />
    </td>
    <td>
      <img width="200" height="600" alt="Detailed" src="https://github.com/RadhapyariDevi/Crop-Disease-Detection-AI/blob/main/Screenshots/healthy%20leaf.jpg" />
    </td>
    <td>
      <img width="200" height="600" alt="easy explanation" src="https://github.com/RadhapyariDevi/Crop-Disease-Detection-AI/blob/main/Screenshots/affected%20leaf.jpg" />
    </td>
  </tr>
</table>

## Tech Stack
- **Android (Kotlin, XML, Android Studio)**  
- **Deep Learning (MobileNet, YOLOv8)**  
- **Python** (model training & optimization)  
- **Firebase** (backend services, authentication, database)

## 📂 Dataset
- Augmented dataset of **87,000+ leaf images**
- Consist of 38 different plant diseases.
- Dataset: [Kaggle](https://www.kaggle.com/datasets/vipoooool/new-plant-diseases-dataset)

## Installation / Setup
1. Clone the repository:  
   ```bash
   git clone https://github.com/RadhapyariDevi/Crop-Disease-Detection-AI
2. Open the project in Android Studio.
3. Configure Firebase in the google-services.json.
4. Build and run on an emulator or Android device.

## License

This project is licensed under the **MIT License**.
