# ArtLensAI

이미지를 업로드하면 AI가 일러스트의 화풍을 분석해, 비슷한 화풍의 작가를 찾아주고
취향에 맞는 작가를 추천해주는 서비스입니다.

🔗 **배포 주소**: [artlensai.kr](https://artlensai.kr)

## 프로젝트 소개

Safebooru 기반 일러스트레이터 데이터를 CLIP 임베딩으로 분석해, 사용자가 업로드한
이미지의 색감·태그·구도 등 화풍 특징을 추출하고 가장 비슷한 작가를 찾아줍니다.
단순 분석에서 끝나지 않고, 찜한 작가와 분석 이력을 바탕으로 사용자 취향 벡터를
학습해 개인화된 작가 추천까지 제공합니다.

## 주요 기능

- **화풍 분석**: 이미지 업로드 → 색상 팔레트, 화풍 태그, 유사 작가 분석
  - 동일 이미지는 재분석 없이 캐시된 결과를 즉시 반환
  - 유저별 일일 분석 횟수 제한으로 API 호출 비용 관리
- **작가 탐색**: Elasticsearch 기반 작가/태그 검색, 자동완성
- **작가 찜(좋아요)**: 마음에 드는 작가를 찜하고 모아보기
- **유사 작가 추천**: pgvector 기반 임베딩 유사도로 취향에 맞는 작가 추천
- **Style Map**: 작가들의 화풍을 좌표 공간에 시각화, 찜한 작가만 필터링해서 보기
- **취향 화풍 테스트**: 질문에 답하며 내 화풍 취향에 가까운 작가 찾기
- **분석결과 공유**: 공개 링크 발급, 카카오톡/페이스북/네이버로 공유
- **분석 히스토리**: 과거 분석 결과 모아보고 관리

## 기술 스택

**Backend**
- Java 17, Spring Boot 3.3
- Spring Security (JWT, OAuth2 Resource Server)
- PostgreSQL (pgvector), MongoDB, Redis, Elasticsearch
- AWS S3 SDK (NCP Object Storage 연동)

**Frontend**
- React 19, Vite
- D3.js (Style Map 시각화)
- Framer Motion

**AI 분석 서버**
- Python, FastAPI, PyTorch, CLIP, scikit-learn, UMAP
- 별도 저장소로 분리 운영 → [ArtLensAI-python](https://github.com/kimyega/ArtLensAI-python)

## 관련 저장소

| 저장소 | 설명 |
|---|---|
| [ArtLensAI-python](https://github.com/kimyega/ArtLensAI-python) | CLIP 기반 화풍 분석 FastAPI 서버 및 데이터 파이프라인 |
