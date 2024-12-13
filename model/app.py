from flask import Flask, request, jsonify
import numpy as np
import tensorflow.lite as tflite
from sklearn.preprocessing import StandardScaler
import os
from google.cloud import storage
import tempfile

app = Flask(__name__)

import tempfile

def download_model_from_gcs(bucket_name, model_filename):
    client = storage.Client()  
    bucket = client.get_bucket(bucket_name)
    blob = bucket.blob(model_filename)

    _, temp_model_file = tempfile.mkstemp(suffix='.tflite')
    blob.download_to_filename(temp_model_file)
    return temp_model_file

bucket_name = 'model-data-capstone'
model_filename = 'model_optimized.tflite'

model_path = download_model_from_gcs(bucket_name, model_filename)

interpreter = tflite.Interpreter(model_path=model_path)
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

X_train = np.array([[58, 0, 0, 100, 122, 0], [52, 1, 0, 125, 168, 0]])

scaler = StandardScaler()
scaler.fit(X_train)

@app.route('/predict', methods=['POST'])
def predict():
    try:
        input_data = np.array(request.json['input'])
        fixed_features_scaled = scaler.transform([input_data])
        test_input = fixed_features_scaled.astype(np.float32)
        interpreter.set_tensor(input_details[0]['index'], test_input)
        interpreter.invoke()
        output_data = interpreter.get_tensor(output_details[0]['index'])
        return jsonify({'prediction': output_data.tolist()})
    except Exception as e:
        return jsonify({'error': str(e)}), 400

@app.route('/', methods=['GET'])
def home():
    return "Optimized TensorFlow Lite Model Deployed on Cloud Run"

if __name__ == '__main__':
    port = int(os.environ.get("PORT", 8080))
    app.run(host='0.0.0.0', port=port)