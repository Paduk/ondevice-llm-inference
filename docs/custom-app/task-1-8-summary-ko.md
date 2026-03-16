# Custom App 진행 요약 (Task 1 - Task 8)

## 문서 목적

이 문서는 현재까지 진행된 `Task 1`부터 `Task 8`까지의 구현 내용을 사람이 빠르게 확인할 수 있도록 한글로 요약한 참고 문서다.

관련 상세 문서는 다음을 기준으로 본다.

- 문서 맵: [README.md](/home/hj153lee/SmolChat-Android/docs/custom-app/README.md)
- 진행 현황: [roadmap.md](/home/hj153lee/SmolChat-Android/docs/custom-app/roadmap.md)

## 현재 상태 요약

현재 `Task 1`부터 `Task 8`까지는 구현 및 컴파일 검증이 끝난 상태다.

이미 연결된 핵심 기능:

- 로컬 GGUF 모델 import
- 시스템 프롬프트 및 추론 파라미터 설정
- compact 앱 진입 구조
- 멀티턴 대화
- 응답 속도/시간/컨텍스트 길이 표시
- 모델 출력 JSON strict 파싱
- TSV gold file strict 로딩
- JSON prediction vs TSV gold 비교
- Macro Accuracy 계산 및 표시

## Task 별 요약

### Task 1. Scope Lock

목적:

- MVP에서 사용할 JSON/TSV/evaluation 기준을 먼저 고정

결정된 내용:

- JSON 필수 키:
  - `query`
  - `rewrited_query`
  - `generated`
  - `answer`
- TSV 필수 컬럼:
  - `conversation_history`
  - `query`
  - `rewrited_query`
  - `answer`
  - `unique_idx`
  - `candidates`
- TSV는 MVP에서 로컬 파일 import 방식 사용
- 평가 기준은 strict equality

의미:

- 이후 parser, loader, evaluator 구현이 임의 판단 없이 진행될 수 있게 기준선을 만든 단계

### Task 2. Compact App Entry Flow

목적:

- 기존 `MainActivity -> DownloadModelActivity / ChatActivity` 진입 흐름을 중단하고 커스텀 앱 플로우로 직접 진입

구현 내용:

- `MainActivity`가 새 compose 기반 custom flow를 직접 띄우도록 변경
- `Setup`, `Chat And Evaluate` 2화면 구조의 기본 navigation 생성

의미:

- 기존 범용 앱 구조 위에 새 커스텀 앱 흐름의 뼈대를 만든 단계

### Task 3. Setup Screen MVP

목적:

- 모델 실행 전에 필요한 설정을 한 화면에서 처리

구현 내용:

- GGUF 파일 import
- import된 모델 목록 조회 및 선택
- 선택 모델 메타데이터 표시
- system prompt 편집
- 추론 파라미터 편집
  - `temperature`
  - `min-p`
  - `context size`
  - `num threads`
  - `use mmap`
  - `use mlock`
- TSV gold file import
- 설정값을 `SharedPrefStore`에 저장

의미:

- “모델 준비 + 평가 준비”를 compact UI 안에서 끝낼 수 있게 된 단계

### Task 4. Multi-turn Chat Core

목적:

- compact 화면에서 실제 멀티턴 추론 가능하게 만들기

구현 내용:

- setup에서 선택한 모델/파라미터를 읽어 session chat 생성 또는 재사용
- user 메시지 저장
- assistant 응답 저장
- 다음 턴 추론 시 기존 chat history를 자동 replay
- reset 버튼으로 대화 초기화 및 모델 재로드
- partial response 표시

의미:

- “1턴 응답이 다음 턴 컨텍스트에 자동 포함되는 구조”가 실제로 연결된 단계

### Task 5. Runtime Metrics

목적:

- 추론 성능과 컨텍스트 사용량을 화면에 표시

구현 내용:

- generation speed (`token/s`) 표시
- generation time (`s`) 표시
- prompt/context length used 표시

의미:

- 요구사항 4번의 핵심 지표가 UI에서 확인 가능해진 단계

### Task 6. JSON Output Parser

목적:

- 모델 출력이 요구된 JSON 형식을 만족하는지 strict하게 확인

구현 내용:

- 단일 JSON object만 허용
- 필수 키 누락 시 parse 실패
- 필수 키가 string이 아니면 parse 실패
- parse 성공 시 normalized prediction 생성
- parse 실패 시 에러 메시지 표시

중요:

- parse 실패하더라도 assistant raw output 자체는 conversation 영역에서 그대로 볼 수 있음

의미:

- 이후 evaluator가 사용할 prediction 구조를 안정적으로 확보한 단계

### Task 7. TSV Gold Loader

목적:

- TSV 정답 파일을 strict하게 로드

구현 내용:

- 필수 컬럼 존재 여부 검증
- `unique_idx` 중복 검증
- `unique_idx` 빈 값 검증
- `answer` 빈 값 검증
- row column 수 검증
- 로드된 record 수 표시
- 로드 실패 시 에러 메시지 표시

의미:

- evaluator가 의존하는 gold 데이터가 신뢰 가능한 형식인지 확인하는 단계

### Task 8. Macro Accuracy Evaluator

목적:

- parsed JSON prediction과 TSV gold를 비교해서 정오와 Macro Accuracy를 계산

구현 내용:

- `query + rewrited_query` 기준으로 TSV row 매칭
- JSON `answer`와 TSV `answer` 비교
- latest per-turn result 계산
- cumulative evaluation history 유지
- cumulative Macro Accuracy 계산
- UI에 평가 결과 표시

표시 항목:

- latest `unique_idx`
- gold answer
- predicted answer
- correct / incorrect
- cumulative Macro Accuracy
- evaluated sample 수

의미:

- 요구사항 3번의 MVP 평가 기능이 실제로 동작하도록 연결된 단계

## 현재 시점에서 가능한 것

현재 코드 기준으로는 아래 흐름이 가능하다.

1. 앱 실행
2. GGUF 모델 import
3. system prompt 및 추론 파라미터 설정
4. TSV gold file import
5. chat 화면 진입
6. 멀티턴 대화 수행
7. 응답 속도/시간/컨텍스트 길이 확인
8. JSON parse 성공/실패 확인
9. TSV 기준 정오 여부 및 Macro Accuracy 확인

## 아직 남은 것

Task 9, Task 10이 남아 있다.

### Task 9

- 기존 legacy flow를 메인 사용자 경로에서 더 제거
- 불필요한 화면/진입점/흐름 정리

### Task 10

- dependency, permission, resource, manifest footprint 정리
- 앱 경량화 및 마무리 정리

## 검증 상태

현재까지는 각 단계마다 `./gradlew :app:compileDebugKotlin` 기준으로 컴파일 검증이 완료됐다.

주의:

- 이건 “코드가 빌드된다”는 뜻이지 “실기기 기능 검증이 끝났다”는 뜻은 아니다.
- 실제 APK 설치 및 단말 테스트는 별도로 진행해야 한다.
