# Python vs APK 추론 차이 메모

## 목적

`/home/hj153lee/RMA/ollama_inference_qwen3_rma.py` 경로와 Android APK 경로의 추론 품질 차이를 유발할 수 있는 설정 차이를 정리한다.

## 가장 큰 차이

### 1. Temperature

- Python:
  - `temperature = 0.0`
- APK 기본값:
  - `temperature = 0.8`

이 차이만으로도 출력 안정성과 정답률 차이가 크게 날 수 있다.

### 2. Prompt 주입 방식

- Python:
  - 완성된 prompt 문자열을 그대로 Ollama `/api/generate`에 전달
- APK:
  - 완성된 문자열을 `query`로 넣고
  - GGUF chat template를 한 번 더 적용해서 모델에 전달

즉 APK 쪽은 현재 batch에서 사실상 "완성 prompt를 user message로 다시 감싸는" 구조다.
이 차이는 품질 차이에 매우 크게 영향을 줄 가능성이 높다.

### 3. Chat template 적용 여부

- Python:
  - Ollama `generate` 경로라 raw prompt 중심
- APK:
  - GGUF에서 읽은 `chatTemplate` 사용
  - 없으면 `SmolLM.DefaultInferenceParams.chatTemplate` 사용

따라서 같은 prompt 텍스트라도 실제 최종 입력 토큰 시퀀스가 다를 수 있다.

### 4. JSON 출력 강제 방식

- Python:
  - Ollama 요청 body에 `"format": "json"`
- APK:
  - 별도의 runtime JSON mode 강제가 없음
  - prompt와 parser만으로 JSON을 기대

이 차이도 출력 품질과 파싱 성공률에 영향을 준다.

### 5. Generation length

- Python:
  - `num_predict = 512`
- APK:
  - 코드상 동일한 generation max token 제한을 찾지 못함

길이 제한 차이 때문에 응답 장황도나 truncation 양상이 달라질 수 있다.

## 추가 차이

### 6. 출력 평가 대상 shape

- Python 평가 경로:
  - `plan`, `arguments`
- APK parser:
  - `query`, `rewrited_query`, `generated`, `answer`

즉 현재 두 경로는 기대하는 JSON shape도 완전히 같지 않다.

### 7. 도구 문자열 source

- Python:
  - 현재 SFT 경로는 `simple_api.json`
- APK:
  - 이제 `simple_api.json`으로 맞춘 상태

이 부분은 최근 수정으로 비교적 정렬되었다.

### 8. Model identity 자체가 다를 가능성

- Python:
  - `OLLAMA_MODEL_NAME = "qwen3-4b-rma-q4km"`
- APK:
  - 사용자가 import한 GGUF 모델에 따라 달라짐

즉 모델 크기나 가중치 자체가 다르면 품질 비교가 성립하지 않는다.

## 우선 확인 순서

1. Python과 APK가 진짜 같은 모델인지 확인
2. APK temperature를 `0.0`으로 맞추기
3. APK prompt가 raw prompt 그대로 들어가는지 확인
4. APK에서 chat template 이중 적용이 있는지 확인
5. JSON 강제 방식 차이 줄이기
6. generation max token 제한 맞추기

## 현재 판단

하드웨어 차이보다 먼저 봐야 할 것은 아래다.

- temperature 차이
- raw prompt vs chat-template wrapping 차이
- JSON mode 강제 차이
- 모델 자체 동일 여부

특히 현재 APK batch 경로의 prompt wrapping 방식은 Python과 상당히 다를 가능성이 높다.
