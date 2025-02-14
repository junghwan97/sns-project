# 📘 텍스트 기반 소셜 네트워크 서비스

**텍스트 기반 소셜 네트워크 서비스**는 대규모 트래픽 환경을 가정한 기본적인 소셜 네트워크 기능을 제공하는 백엔드 애플리케이션입니다.  
사용자는 텍스트 기반 게시물을 작성하고, 다른 사용자를 팔로우하거나 게시물에 좋아요와 댓글을 남길 수 있습니다.

---

## 📌 프로젝트 개요

- **목적**:  
  대규모 트래픽을 처리할 수 있는 소셜 네트워크 백엔드 시스템 설계 및 구현 학습.  

- **특징**:  
  - 게시물 관리(작성, 조회, 수정, 삭제).  
  - 팔로우 기반 피드 제공.  
  - 동시성 및 데이터 정합성 문제를 해결한 좋아요/댓글 기능.  
  - MySQL 성능 최적화를 통한 효율적인 데이터 관리.

---

## 🔧 기술 스택

- **Backend**: `Java` `Spring Boot` `Spring Security`  
- **Databases & ORM Tools**: `MySQL` `JPA`  
- **Cloud & Infrastructure**: `AWS-EC2` `Nginx`  
- **Authentication**: `JWT`

---

## 🏛 아키텍처 설계

### 서비스 구성

- **Nginx**를 이용한 로드 밸런싱으로 서버 부하를 분산.  
- **Spring Boot** 기반 REST API 설계.  
- **MySQL**을 사용한 관계형 데이터베이스 설계 및 성능 최적화.  

### 아키텍처 다이어그램

```plaintext
Client
  |
  v
Nginx (로드 밸런싱)
  |
  v
Spring Boot Application
  |
  +--> Authentication (JWT)
  |
  +--> Business Logic (Post, Follow, Feed)
  |
  v
MySQL Database
```

## ✨ 구현 기능

1. **회원가입 및 로그인**
   - **JWT 인증**을 활용하여 회원 정보를 유지하고, 다중 서버 환경에서도 일관된 인증 처리.  
   - 회원가입 시 유효성 검사를 수행하여 데이터의 정확성을 보장.  

2. **포스트 관리**
   - 게시물 작성, 수정, 삭제, 조회 기능을 구현.  
   - **팔로우 기반 피드**를 통해 자신이 팔로우한 사용자의 게시물만 조회 가능.  

3. **좋아요 및 댓글**
   - 게시물에 좋아요를 추가하고, 동시성 문제를 해결하여 데이터 정합성을 유지.  
   - 댓글 기능을 통해 사용자 간 상호작용 강화.  

4. **팔로우 기능**
   - 회원 간 팔로우 및 언팔로우 기능.  
   - 팔로우 정보와 회원 상태(탈퇴 여부)를 효율적으로 조회하도록 데이터베이스 최적화.  

---

## 🚀 트러블 슈팅

### 1. 좋아요 동시성 이슈

- **문제**:  
  한 게시물에 여러 사용자가 동시에 좋아요를 누를 경우 일부 요청이 누락되어 좋아요 카운트가 정확하지 않은 문제 발생.  

- **기술적 의사결정**:  
  - **비관적 락**: 데이터 정합성은 확보되지만, 성능 저하 우려로 제외.  
  - **Redis 사용**: 고성능 처리 가능하지만, 초기 구축 및 유지보수 부담으로 제외.  
  - **낙관적 락**: 충돌 발생 시 재처리 로직 추가로 데이터 유실을 방지하면서 성능 유지.  

- **결과**:  
  - 낙관적 락 적용 후 데이터 정합성 문제 해결.  
  - 성능 저하 없이 동시성 이슈를 안정적으로 처리.  

---

### 2. 팔로우 피드 최적화

- **문제**:  
  팔로우 정보를 조회할 때, **회원 탈퇴 여부 확인** 과정에서 불필요한 디스크 I/O가 발생하여 조회 속도가 느려짐.  

- **기술적 의사결정**:  
  - **커버링 인덱스 생성**: 팔로우 정보와 회원 상태를 동시에 조회하도록 설계.  
  - 쿼리 최적화를 통해 불필요한 디스크 접근을 최소화.  

- **결과**:  
  - 팔로우 피드 조회 성능이 **56% 향상**.  
  - 불필요한 데이터 접근 제거로 조회 효율성 증가.  

---

### 3. 페이지네이션 성능 문제

- **문제**:  
  오프셋 기반 페이지네이션에서 데이터 증가로 인해 조회 속도가 점점 느려지고, 중복 게시글이 발생하는 문제.  

- **기술적 의사결정**:  
  - **커서 기반 페이지네이션 도입**:  
    - 마지막 조회된 데이터의 ID를 기준으로 이후 데이터를 가져오는 방식.  
    - 데이터 중복 발생을 방지하기 위한 추가 로직 구현.  

- **결과**:  
  - 데이터베이스 부하 감소 및 중복 게시글 문제 해결.  
  - 페이지네이션 성능과 사용성 모두 개선.  


