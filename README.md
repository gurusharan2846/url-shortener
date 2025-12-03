# URL Shortener (Distributed System)

A production-grade distributed URL shortener built with:
- Java / Spring Boot
- Cassandra (distributed storage)
- Redis (caching)
- Kafka (event-driven analytics)
- Kubernetes (scaling + orchestration)
- Prometheus & Grafana (monitoring)
- GitHub Actions (CI/CD)

## Repository Structure
services/
  api/
  redirect/
  analytics-consumer/

infra/
  docker/
  k8s/

monitoring/
  prometheus/
  grafana/

docs/
  ARCHITECTURE.md
