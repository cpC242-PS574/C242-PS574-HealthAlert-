FROM python:3.10-slim

WORKDIR /app

COPY app.py /app/

RUN pip install --no-cache-dir flask numpy tensorflow==2.17.0 scikit-learn google-cloud-storage

EXPOSE 8080

CMD ["python", "app.py"]