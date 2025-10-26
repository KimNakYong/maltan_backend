#!/bin/bash

echo "================================================"
echo "🔍 Docker 컨테이너 상태 확인"
echo "================================================"

echo -e "\n=== 1. 실행 중인 컨테이너 목록 ==="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo -e "\n=== 2. 모든 컨테이너 상태 (중지된 것 포함) ==="
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo -e "\n=== 3. 각 서비스 상세 확인 ==="

services=("gateway-service" "user-service" "place-service" "community-service" "recommendation-service" "monitoring-service")

for service in "${services[@]}"; do
    echo -e "\n--- $service ---"
    if docker ps --format '{{.Names}}' | grep -q "^${service}$"; then
        echo "✅ 상태: 실행 중"
        echo "📊 리소스 사용량:"
        docker stats "$service" --no-stream --format "  CPU: {{.CPUPerc}}  Memory: {{.MemUsage}}"
        echo "📝 최근 로그 (마지막 10줄):"
        docker logs "$service" --tail=10 2>&1 | sed 's/^/  /'
    else
        echo "❌ 상태: 중지됨 또는 없음"
        if docker ps -a --format '{{.Names}}' | grep -q "^${service}$"; then
            echo "📝 종료 로그 (마지막 20줄):"
            docker logs "$service" --tail=20 2>&1 | sed 's/^/  /'
        fi
    fi
done

echo -e "\n=== 4. Docker 네트워크 확인 ==="
docker network ls | grep maltan

echo -e "\n=== 5. 포트 사용 현황 ==="
echo "Gateway: 8080"
netstat -tuln | grep :8080 || echo "  ❌ 8080 포트 사용 안함"

echo "User: 8081"
netstat -tuln | grep :8081 || echo "  ❌ 8081 포트 사용 안함"

echo "Place: 8082"
netstat -tuln | grep :8082 || echo "  ❌ 8082 포트 사용 안함"

echo "Community: 8083"
netstat -tuln | grep :8083 || echo "  ❌ 8083 포트 사용 안함"

echo "Recommendation: 8084"
netstat -tuln | grep :8084 || echo "  ❌ 8084 포트 사용 안함"

echo "Monitoring: 8085"
netstat -tuln | grep :8085 || echo "  ❌ 8085 포트 사용 안함"

echo -e "\n=== 6. 데이터베이스 연결 확인 ==="
echo "MySQL (3306):"
netstat -tuln | grep :3306 && echo "  ✅ MySQL 실행 중" || echo "  ❌ MySQL 중지됨"

echo "PostgreSQL (5432):"
netstat -tuln | grep :5432 && echo "  ✅ PostgreSQL 실행 중" || echo "  ❌ PostgreSQL 중지됨"

echo "Redis (6379):"
netstat -tuln | grep :6379 && echo "  ✅ Redis 실행 중" || echo "  ❌ Redis 중지됨"

echo -e "\n=== 7. Docker Compose 상태 (docker 디렉토리에서) ==="
cd ~/maltan-backend/docker 2>/dev/null && docker-compose ps || echo "  ⚠️  docker-compose.yml 위치 확인 필요"

echo -e "\n================================================"
echo "✅ 서비스 상태 체크 완료"
echo "================================================"

